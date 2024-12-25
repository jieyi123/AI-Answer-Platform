import { RouteRecordRaw } from "vue-router";
import ACCESS_ENUM from "@/access/accessEnum";
import UserLoginPage from "@/views/user/UserLoginPage.vue";
import UserRegisterPage from "@/views/user/UserRegisterPage.vue";
import AdminUserPage from "@/views/admin/AdminUserPage.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import HomePage from "@/views/HomePage.vue";
export const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "主页",
        component: HomePage,
    },
    {
        path: "/admin/user",
        name: "用户管理",
        component: AdminUserPage,
        meta: {
            access: ACCESS_ENUM.ADMIN,
        },
    },
    {
        path: "/user",
        name: "用户",
        component: UserLayout,
        children: [
            {
                path: "/user/login",
                name: "用户登录",
                component: UserLoginPage,
            },
            {
                path: "/user/register",
                name: "用户注册",
                component: UserRegisterPage,
            },
        ],
        meta: {
            hideInMenu: true,
        },
    }
];