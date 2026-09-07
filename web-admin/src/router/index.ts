import { createRouter, createWebHistory } from 'vue-router'
import Index from '../views/Index.vue'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import EmployeeManagement from '../views/EmployeeManagement.vue'
import EmployeeDetail from '../views/EmployeeDetail.vue'
import EmployeeSummary from '../views/EmployeeSummary.vue'
import ChatAssistant from '../views/ChatAssistant.vue'
import UpdatesManagement from '../views/UpdatesManagement.vue'
import ServiceIntroManagement from '../views/ServiceIntroManagement.vue'
import HotPicksManagement from '../views/HotPicksManagement.vue'
import AboutManagement from '../views/AboutManagement.vue'
import HousekeeperManagement from '../views/HousekeeperManagement.vue'
import HousekeeperScheduleManagement from '../views/HousekeeperScheduleManagement.vue'
import CategoryManagement from '../views/CategoryManagement.vue'
import ServiceManagement from '../views/ServiceManagement.vue'
import ServiceForm from '../views/ServiceForm.vue'
import ServicePackageManagement from '../views/ServicePackageManagement.vue'
import ServicePackageForm from '../views/ServicePackageForm.vue'
import CouponManagement from '../views/CouponManagement.vue'
import EmployeeLogin from '../views/EmployeeLogin.vue'
import EmployeeHome from '../views/EmployeeHome.vue'

const routes = [
  // ---- 入口 ----
  {
    path: '/',
    name: 'Index',
    component: Index
  },
  // ---- 管理端 ----
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, portal: 'admin' }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true, portal: 'admin' },
    children: [
      {
        path: '/employee',
        name: 'EmployeeManagement',
        component: EmployeeManagement,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/employee/detail',
        name: 'EmployeeDetail',
        component: EmployeeDetail,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/employee/summary',
        name: 'EmployeeSummary',
        component: EmployeeSummary,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/employee/chat',
        name: 'ChatAssistant',
        component: ChatAssistant,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/updates',
        name: 'UpdatesManagement',
        component: UpdatesManagement,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/service-intro',
        name: 'ServiceIntroManagement',
        component: ServiceIntroManagement,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/hot-picks',
        name: 'HotPicksManagement',
        component: HotPicksManagement,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/about',
        name: 'AboutManagement',
        component: AboutManagement,
        meta: { requiresAuth: true, portal: 'admin' }
      },
      {
        path: '/housekeeper',
        name: 'HousekeeperManagement',
        component: HousekeeperManagement,
        meta: { requiresAuth: true, portal: 'admin' }
      },
    ]
  },
  // ---- 员工端 ----
  {
    path: '/employee/login',
    name: 'EmployeeLogin',
    component: EmployeeLogin,
    meta: { requiresAuth: false, portal: 'employee' }
  },
  {
    path: '/employee/home',
    name: 'EmployeeHome',
    component: EmployeeHome,
    meta: { requiresAuth: true, portal: 'employee' },
    children: [
      {
        path: '/employee/category',
        name: 'EmployeeCategoryManagement',
        component: CategoryManagement,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/service-item',
        name: 'ServiceManagement',
        component: ServiceManagement,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/service-item/add',
        name: 'ServiceFormAdd',
        component: ServiceForm,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/service-item/edit',
        name: 'ServiceFormEdit',
        component: ServiceForm,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/service-package',
        name: 'ServicePackageManagement',
        component: ServicePackageManagement,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/service-package/add',
        name: 'ServicePackageFormAdd',
        component: ServicePackageForm,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/service-package/edit',
        name: 'ServicePackageFormEdit',
        component: ServicePackageForm,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/housekeeper-schedule',
        name: 'HousekeeperScheduleManagement',
        component: HousekeeperScheduleManagement,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
      {
        path: '/employee/voucher',
        name: 'CouponManagement',
        component: CouponManagement,
        meta: { requiresAuth: true, portal: 'employee', roles: ['admin', 'manager'] }
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：支持管理端和员工端双通道认证 + 角色权限
router.beforeEach((to, _from, next) => {
  const portal = (to.meta.portal as string) || 'admin'

  const tokenKey = portal === 'employee' ? 'employeeToken' : 'token'
  const expireKey = portal === 'employee' ? 'employeeTokenExpireTime' : 'tokenExpireTime'
  const loginPath = portal === 'employee' ? '/employee/login' : '/login'

  const token = sessionStorage.getItem(tokenKey)
  const tokenExpireTime = sessionStorage.getItem(expireKey)

  const isTokenValid = token && tokenExpireTime && Date.now() < parseInt(tokenExpireTime)

  if (to.meta.requiresAuth) {
    if (!isTokenValid) {
      sessionStorage.removeItem(tokenKey)
      sessionStorage.removeItem(expireKey)
      next(loginPath)
      return
    }

    // 角色权限校验（员工端）
    if (portal === 'employee') {
      const requiredRoles = to.meta.roles as string[] | undefined
      if (requiredRoles && requiredRoles.length > 0) {
        const currentRole = sessionStorage.getItem('employeeRole') || 'staff'
        if (!requiredRoles.includes(currentRole)) {
          alert('权限不足，无法访问该页面')
          next('/employee/home')
          return
        }
      }
    }

    next()
  } else {
    next()
  }
})

export default router
