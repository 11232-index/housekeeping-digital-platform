var app = getApp();

var WELCOME = '您好，我是智能家政客服小助手，可以帮您查询服务价格、预约订单、优惠券和售后问题。请问有什么可以帮您？';

var QUICK_QUESTIONS = [
  '日常保洁多少钱？',
  '我的订单到哪一步了？',
  '优惠券怎么用？',
  '阿姨迟到了怎么办？'
];

// SSE 的 chunk 可能把汉字拆到两个包里，这里做增量 UTF-8 解码
function decodeUtf8Chunk(prevBytes, arrayBuffer) {
  var incoming = new Uint8Array(arrayBuffer);
  var bytes = prevBytes.concat(Array.prototype.slice.call(incoming));
  var text = '';
  var i = 0;

  while (i < bytes.length) {
    var b = bytes[i];
    var need = 0;

    if (b < 0x80) {
      text += String.fromCharCode(b);
      i += 1;
      continue;
    } else if ((b & 0xE0) === 0xC0) {
      need = 1;
    } else if ((b & 0xF0) === 0xE0) {
      need = 2;
    } else if ((b & 0xF8) === 0xF0) {
      need = 3;
    } else {
      i += 1;
      continue;
    }

    if (i + need >= bytes.length) break;

    var cp = b & (0x3F >> need);
    for (var j = 1; j <= need; j++) {
      cp = (cp << 6) | (bytes[i + j] & 0x3F);
    }

    if (cp > 0xFFFF) {
      cp -= 0x10000;
      text += String.fromCharCode(0xD800 + (cp >> 10), 0xDC00 + (cp & 0x3FF));
    } else {
      text += String.fromCharCode(cp);
    }
    i += need + 1;
  }

  return { text: text, remaining: bytes.slice(i) };
}

// 小程序不渲染 Markdown，简单去掉加粗、标题、行内代码标记
function cleanMarkdown(text) {
  return String(text || '')
    .replace(/\*\*(.*?)\*\*/g, '$1')
    .replace(/^#{1,6}\s*/gm, '')
    .replace(/`([^`]*)`/g, '$1')
    .replace(/^\s*[-*]\s+/gm, '• ');
}

Page({
  data: {
    messages: [],
    inputValue: '',
    sending: false,
    scrollTop: 0,
    sessionId: '',
    userAvatar: '/images/logo.png',
    quickQuestions: QUICK_QUESTIONS,
    aiBaseUrl: ''
  },

  onLoad: function () {
    var userInfo = wx.getStorageSync('userInfo') || {};
    var sid = wx.getStorageSync('cs_session_id') || '';
    if (!sid) {
      sid = 'mp_' + Date.now() + '_' + Math.floor(Math.random() * 10000);
      wx.setStorageSync('cs_session_id', sid);
    }

    this.setData({
      sessionId: sid,
      userAvatar: userInfo.avatarUrl || '/images/logo.png',
      aiBaseUrl: (app.globalData && app.globalData.aiBaseUrl) || 'http://192.168.137.1:5000',
      messages: [{
        id: 'welcome',
        role: 'assistant',
        content: WELCOME,
        loading: false,
        agent: ''
      }]
    });

    this.scrollToBottom();
  },

  onUnload: function () {
    if (this.requestTask) {
      try { this.requestTask.abort(); } catch (e) { /* ignore */ }
      this.requestTask = null;
    }
  },

  onInput: function (e) {
    this.setData({ inputValue: e.detail.value });
  },

  onQuickQuestion: function (e) {
    var q = e.currentTarget.dataset.q || '';
    this.setData({ inputValue: q }, function () {
      this.sendMessage();
    }.bind(this));
  },

  sendMessage: function () {
    var text = (this.data.inputValue || '').trim();
    if (!text || this.data.sending) return;

    var now = Date.now();
    var messages = this.data.messages.concat([
      { id: 'u_' + now, role: 'user', content: text, loading: false, agent: '' },
      { id: 'a_' + now, role: 'assistant', content: '', loading: true, agent: '', steps: [] }
    ]);
    var aiIndex = messages.length - 1;

    this.setData({
      messages: messages,
      inputValue: '',
      sending: true
    }, function () {
      this.scrollToBottom();
    }.bind(this));

    this.startStream(text, aiIndex);
  },

  startStream: function (text, aiIndex) {
    var that = this;
    var hadChunk = false;

    // 每次请求一个序号：旧请求迟到的回调直接丢弃，避免串流
    var seq = (this._streamSeq || 0) + 1;
    this._streamSeq = seq;

    // 上一条请求如果还没结束，先中止，避免两条流共用缓冲区
    if (this.requestTask) {
      try { this.requestTask.abort(); } catch (e) {}
      this.requestTask = null;
    }

    this._sseBuffer = '';
    this._utf8Remaining = [];
    this._streamFinished = false;

    var task = wx.request({
      url: this.data.aiBaseUrl + '/api/chat/stream',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'token': wx.getStorageSync('token') || ''
      },
      data: {
        message: text,
        session_id: this.data.sessionId
      },
      enableChunked: true,
      responseType: 'arraybuffer',
      timeout: 120000,
      success: function () {
        if (seq !== that._streamSeq) return;
        // 把最后残留的 SSE 内容解析掉
        if (that._sseBuffer) that.consumeSseBuffer(aiIndex);

        if (!hadChunk) {
          // 当前基础库不支持分块接收时，回退到非流式接口
          that.fallbackNonStreaming(text, aiIndex, seq);
        } else {
          that.finishStream(aiIndex);
        }
      },
      fail: function (err) {
        if (seq !== that._streamSeq) return;
        console.log('AI stream request failed:', err);
        that.setMessageContent(aiIndex, '抱歉，智能客服暂时连接不上。请确认 Flask 服务已启动（端口 5000），或稍后再试。');
        that.finishStream(aiIndex);
      }
    });

    this.requestTask = task;

    if (typeof task.onChunkReceived !== 'function') {
      return;
    }

    task.onChunkReceived(function (res) {
      if (seq !== that._streamSeq) return;
      hadChunk = true;
      var decoded = decodeUtf8Chunk(that._utf8Remaining, res.data);
      that._utf8Remaining = decoded.remaining;
      that._sseBuffer += decoded.text.replace(/\r\n/g, '\n');
      that.consumeSseBuffer(aiIndex);
    });
  },

  consumeSseBuffer: function (aiIndex) {
    var buffer = this._sseBuffer || '';
    var idx;

    while ((idx = buffer.indexOf('\n\n')) >= 0) {
      var block = buffer.slice(0, idx);
      buffer = buffer.slice(idx + 2);
      this.handleSseBlock(block, aiIndex);
    }

    this._sseBuffer = buffer;
  },

  handleSseBlock: function (block, aiIndex) {
    var lines = block.split('\n');

    for (var i = 0; i < lines.length; i++) {
      var line = lines[i];
      if (line.indexOf('data:') !== 0) continue;

      var raw = line.slice(5).trim();
      if (!raw || raw === '[DONE]') continue;

      var payload;
      try {
        payload = JSON.parse(raw);
      } catch (e) {
        continue;
      }

      if (payload.session_id) {
        this.setData({ sessionId: payload.session_id });
        wx.setStorageSync('cs_session_id', payload.session_id);
      }

      if (payload.error) {
        this.setMessageContent(aiIndex, '抱歉，客服系统出现错误：' + payload.error);
        continue;
      }

      // 复杂路径：计划与步骤进度
      if (payload.type === 'plan' || payload.type === 'step') {
        this.applyStepEvent(aiIndex, payload);
        continue;
      }

      // 回答已生成完毕（后端在 checkpoint 之前发），先收尾，不等 done
      if (payload.type === 'answer_done') {
        this.handleAnswerDone(aiIndex, payload);
        continue;
      }

      // done 事件有时会带上兜底全文（例如越界固定回复），先追加再结束
      if (payload.content && !this._streamFinished) {
        var current = (this.data.messages[aiIndex] && this.data.messages[aiIndex].content) || '';
        var patch = {};
        patch['messages[' + aiIndex + '].content'] = current + payload.content;
        patch['messages[' + aiIndex + '].loading'] = false;
        if (payload.agent) {
          patch['messages[' + aiIndex + '].agent'] = payload.agent;
        }
        this.setData(patch);
        this.scrollToBottom();
      }

      if (payload.done) {
        if (payload.agent) {
          var agentPatch = {};
          agentPatch['messages[' + aiIndex + '].agent'] = payload.agent;
          this.setData(agentPatch);
        }
        this.finishStream(aiIndex);
        continue;
      }
    }
  },

  // 计划 / 步骤进度事件
  applyStepEvent: function (aiIndex, payload) {
    var msg = this.data.messages[aiIndex] || {};
    var steps = (msg.steps || []).slice();

    if (payload.type === 'plan') {
      steps = (payload.steps || []).map(function (s) {
        return {
          step_id: s.step_id,
          title: s.title,
          status: s.status || 'pending',
          icon: '•'
        };
      });
    } else if (payload.type === 'step') {
      var status = payload.status || 'pending';
      var item = {
        step_id: payload.step_id,
        title: payload.title || '执行中',
        status: status,
        icon: status === 'running' ? '…' : (status === 'done' ? '✓' : (status === 'error' ? '!' : '•'))
      };
      var found = false;
      for (var i = 0; i < steps.length; i++) {
        if (steps[i].step_id === payload.step_id) {
          steps[i] = item;
          found = true;
          break;
        }
      }
      if (!found) steps.push(item);
    }

    var patch = {};
    patch['messages[' + aiIndex + '].steps'] = steps;
    this.setData(patch);
    this.scrollToBottom();
  },

  setMessageContent: function (aiIndex, content) {
    var patch = {};
    patch['messages[' + aiIndex + '].content'] = content;
    patch['messages[' + aiIndex + '].loading'] = false;
    this.setData(patch);
  },

  // 后端回答生成完毕的提前通知：立即显示 expert 标签并恢复发送按钮
  handleAnswerDone: function (aiIndex, payload) {
    if (this._streamFinished) return;

    var msg = this.data.messages[aiIndex] || {};
    var patch = {};
    if (payload.agent) {
      patch['messages[' + aiIndex + '].agent'] = payload.agent;
    }
    // 非流式（没有收到 token）时，正文在这里补上
    if (!msg.content && payload.content) {
      patch['messages[' + aiIndex + '].content'] = payload.content;
      patch['messages[' + aiIndex + '].loading'] = false;
    }
    if (Object.keys(patch).length > 0) this.setData(patch);

    this.finishStream(aiIndex);
  },

  finishStream: function (aiIndex) {
    if (this._streamFinished) return;
    this._streamFinished = true;

    var msg = this.data.messages[aiIndex] || {};
    var content = cleanMarkdown(msg.content);

    var patch = {};
    patch['messages[' + aiIndex + '].content'] = content || '抱歉，暂时没有获取到回复，请稍后再试。';
    patch['messages[' + aiIndex + '].loading'] = false;
    patch.sending = false;

    // 结束时把仍在执行的步骤收敛为完成，避免“…”一直转
    var steps = (msg.steps || []).map(function (s) {
      if (s.status === 'running') {
        return { step_id: s.step_id, title: s.title, status: 'done', icon: '✓' };
      }
      return s;
    });
    if (steps.length) patch['messages[' + aiIndex + '].steps'] = steps;

    this.setData(patch, function () {
      this.scrollToBottom();
    }.bind(this));

    this.requestTask = null;
  },

  fallbackNonStreaming: function (text, aiIndex, seq) {
    var that = this;

    this.requestTask = wx.request({
      url: this.data.aiBaseUrl + '/api/chat',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'token': wx.getStorageSync('token') || ''
      },
      data: {
        message: text,
        session_id: this.data.sessionId
      },
      timeout: 120000,
      success: function (res) {
        if (seq !== that._streamSeq) return;
        var data = res.data || {};

        if (data.session_id) {
          that.setData({ sessionId: data.session_id });
          wx.setStorageSync('cs_session_id', data.session_id);
        }

        that.setMessageContent(aiIndex, data.response || '抱歉，暂时没有获取到回复，请稍后再试。');
        that.finishStream(aiIndex);
      },
      fail: function () {
        if (seq !== that._streamSeq) return;
        that.setMessageContent(aiIndex, '抱歉，智能客服暂时连接不上，请稍后再试。');
        that.finishStream(aiIndex);
      }
    });
  },

  scrollToBottom: function () {
    this._scrollTick = (this._scrollTick || 0) + 1;
    this.setData({ scrollTop: 100000 + this._scrollTick });
  }
});
