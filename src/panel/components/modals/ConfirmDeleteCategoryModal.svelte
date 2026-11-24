<!-- Confirm Delete Category Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered" role="dialog">
    <div class="modal-content">
      <div class="modal-body text-center">
        <div class="pb-3">
          <i class="fas fa-question-circle fa-3x d-block m-auto text-gray"></i>
        </div>
        {@html $_('modals.delete-category.description', { values: { title: escapeHtml($category.title) } })}
      </div>
      <div class="modal-footer flex-nowrap">
        <button class="btn btn-link col-6 m-0" type="button" class:disabled={loading} on:click={hide}>
          {$_('modals.delete-category.cancel')}
        </button>
        <button class="btn btn-danger col-6 m-0" type="button" class:disabled={loading} on:click={onYesClick}>
          {$_('modals.delete-category.confirm')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
  import { get, writable } from 'svelte/store';
  import { api } from '@panomc/sdk/plugin-api';

  const modalElement = writable();
  const category = writable({});
  let callback = (cat) => {};
  let modal;

  export function show(cat) {
    category.set(cat);
    modal = new window.bootstrap.Modal(get(modalElement), {
      backdrop: 'static',
      keyboard: false,
    });
    modal.show();
  }

  export function setCallback(newCallback) {
    callback = newCallback;
  }

  export function hide() {
    modal.hide();
  }
</script>

<script>
  import { _, pluginId, showSuccessToast, showErrorToast, translateError, generalError } from '../../../main';
  import { escapeHtml } from '../../escapeHtml.js';
  let loading = false;

  async function onYesClick() {
    loading = true;
    try {
      const result = await api.panel.delete({
        path: `/media-categories/${get(category).id}`,
      });

      if (result.error) {
        showErrorToast(`plugins.${pluginId}.toasts.error-deleting`, { error: translateError(result.error) });
      } else {
        showSuccessToast(`plugins.${pluginId}.toasts.category-deleted`);
        callback(get(category));
        hide();
      }
    } catch (e) {
      console.error(e);
      showErrorToast(`plugins.${pluginId}.toasts.error-deleting`, {
        error: generalError(),
      });
    } finally {
      loading = false;
    }
  }
</script>
