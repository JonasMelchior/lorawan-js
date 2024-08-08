import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/password-field/theme/lumo/vaadin-password-field.js';
import '@vaadin/email-field/theme/lumo/vaadin-email-field.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'f7fe2707d4322cce040937391f5082597da20c1e0a1f63742e88f9fdde9a5541') {
    pending.push(import('./chunks/chunk-279059c4378a41f95704e977dd9d66ba7cf47572fd73c013ff92baa99c2ffcdf.js'));
  }
  if (key === '80576ddb4586a92eb950c5f9bc435b73b5262c572de13b1010e6fd910ed17330') {
    pending.push(import('./chunks/chunk-03f77cd44b4a0462cd119c886017e71d4aea98bed6d3ce6492b1cbbed84aa0e4.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}