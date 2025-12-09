import { createApp } from 'vue/dist/vue.esm-bundler.js'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入自定义样式系统
import '@/styles/variables.css'
import '@/styles/base.css'
import '@/styles/components.css'

// 引入组件库
import components from '@/components'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册全局组件
app.use(components)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
