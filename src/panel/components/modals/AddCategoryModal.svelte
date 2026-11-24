<!-- Add Category Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered" role="dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">{$_('modals.add-category.title')}</h5>
        <button type="button" class="btn-close" on:click={hide} aria-label={$_('modals.close')}></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label for="cat-title" class="form-label">{$_('modals.add-category.fields.title')}</label>
          <input type="text" class="form-control" id="cat-title" bind:value={title} />
        </div>
        <div class="mb-3">
          <label for="cat-description" class="form-label">{$_('modals.add-category.fields.description')}</label>
          <textarea class="form-control" id="cat-description" rows="2" bind:value={description}></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-link" type="button" class:disabled={loading} on:click={hide}>
          {$_('modals.add-category.cancel')}
        </button>
        <button class="btn btn-secondary" type="button" class:disabled={loading || !title.trim()} on:click={onSave}>
          {#if loading}
            <span class="spinner-border spinner-border-sm me-2" role="status"></span>
          {/if}
          {$_('modals.add-category.save')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
  import { get, writable } from 'svelte/store';
  import { api } from '@panomc/sdk/plugin-api';

  const modalElement = writable();
  let pageId = null;
  let callback = (category) => {};
  let modal;

  export function show(newPageId) {
    pageId = newPageId;

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

  let loading = false;
  let title = '';
  let description = '';

  async function onSave() {
    loading = true;
    try {
      if (!pageId) {
        callback({ id: `temp_${Date.now()}`, pageId: null, title, description, sortOrder: 0, visible: true });
        title = '';
        description = '';
        hide();
        return;
      }

      const result = await api.panel.post({
        path: `/media-pages/${pageId}/categories`,
        body: { title, description },
      });

      if (result.error) {
        showErrorToast(`plugins.${pluginId}.toasts.error-saving`, { error: translateError(result.error) });
      } else {
        const data = result.data || result;
        callback({ id: data.id, pageId, title, description, sortOrder: 0 });
        showSuccessToast(`plugins.${pluginId}.toasts.category-added`);
        title = '';
        description = '';
        hide();
      }
    } catch (e) {
      console.error(e);
      showErrorToast(`plugins.${pluginId}.toasts.error-saving`, {
        error: generalError(),
      });
    } finally {
      loading = false;
    }
  }
</script>
