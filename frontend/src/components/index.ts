import type { App } from 'vue'
import BaseButton from './BaseButton.vue'
import BaseInput from './BaseInput.vue'
import BaseForm from './BaseForm.vue'

export {
  BaseButton,
  BaseInput,
  BaseForm
}

export default {
  install(app: App) {
    app.component('BaseButton', BaseButton)
    app.component('BaseInput', BaseInput)
    app.component('BaseForm', BaseForm)
  }
}