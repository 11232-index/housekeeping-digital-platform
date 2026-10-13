package com.smart.mapper;

import com.smart.annotation.AutoFill;
import com.smart.dto.HousekeeperPageQueryDTO;
import com.smart.entity.Housekeeper;
import com.smart.enumeration.OperationType;
import com.smart.vo.HousekeeperCandidateVO;
import org.apache.ibatis.annotations.*;
import com.github.pagehelper.Page;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface HousekeeperMapper {

    /**
     * 新增家政员（员工role=cleaner时自动调用）
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into housekeeper (employee_id, name, picture, price, rating, `rank`, carousel, review, create_user, update_user, create_time, update_time) values " +
            "(#{employeeId}, #{name}, #{picture}, #{price}, #{rating}, #{rank}, #{carousel}, #{review}, #{createUser}, #{updateUser}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Housekeeper housekeeper);

    /**
     * 分页查询家政员
     */
    Page<Housekeeper> pageQuery(HousekeeperPageQueryDTO dto);

    /**
     * 根据id查询
     */
    @Select("select * from housekeeper where id = #{id}")
    Housekeeper getById(Long id);

    /**
     * 根据员工id查询
     */
    @Select("select * from housekeeper where employee_id = #{employeeId}")
    Housekeeper getByEmployeeId(Long employeeId);

    /**
     * 修改家政员
     */
    @AutoFill(OperationType.UPDATE)
    void update(Housekeeper housekeeper);

    /**
     * 根据员工id删除家政员（员工role从cleaner改为其他时调用）
     */
    @Delete("delete from housekeeper where employee_id = #{employeeId}")
    void deleteByEmployeeId(Long employeeId);

    /**
     * 查询「在某天某时段内空闲」的家政员，用于员工端指派候选。
     * <p>
     * 空闲判定：该家政员在该日期<b>没有任何</b>与订单时段重叠的排期记录 ——
     * 注意这里<b>不看 status</b>（1可约 / 0已占用都算冲突）。
     * 原因：排期表里的「可约」时段同时挂在 C 端供用户下单预约，如果员工把同一时段指派出去，
     * 两边的占用逻辑（一边是精确匹配 CAS，一边是插入新行）对不上，会出现同一家政员同一时段被卖两次。
     * 所以只要重叠就排除，宁可候选少一些。
     * <p>
     * 时段重叠判定：s.start_time &lt; 订单结束 且 s.end_time &gt; 订单开始（半开区间，首尾相接不算冲突）。
     *
     * @param date              预约日期
     * @param startTime         服务时段开始
     * @param endTime           服务时段结束
     * @param excludeScheduleId 需要忽略的排期id —— 传订单当前关联的那条（orders.housekeeper_schedule_id）。
     *                          重新指派时必须忽略它：拒单时已经把这条释放回「可约」了，如果不忽略，
     *                          刚拒单的那位家政员会因为自己这条记录被排除掉，出现「他没在忙却派不了他」的死结。
     *                          首次指派（订单还没关联排期）传 null，条件自动失效。
     * @return 候选家政员列表（按身价、评分排序）
     */
    @Select("select h.id as housekeeperId, h.name, h.picture, h.price, h.rating, h.`rank` as `rank` " +
            "from housekeeper h " +
            "where not exists (select 1 from housekeeper_schedule s " +
            "  where s.housekeeper_id = h.id and s.schedule_date = #{date} " +
            "    and s.start_time < #{endTime} and s.end_time > #{startTime} " +
            "    and (#{excludeScheduleId} is null or s.id != #{excludeScheduleId})) " +
            "order by h.`rank` asc, h.rating desc")
    List<HousekeeperCandidateVO> listCandidates(@Param("date") LocalDate date,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime,
                                                @Param("excludeScheduleId") Long excludeScheduleId);

}
