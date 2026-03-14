<!-- Edit Media Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered" role="dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">{$_('modals.edit-media.title')}</h5>
        <button type="button" class="btn-close" on:click={hide} aria-label="Close"></button>
      </div>
      <div class="modal-body">
        {#if $item.type === 'IMAGE' && ($item.thumbnailPath || $item.filePath)}
          <div class="text-center mb-3">
            <img
              src="/api/media/file/{$item.thumbnailPath || $item.filePath}"
              alt={$item.title}
              class="img-fluid rounded"
              style="max-height: 200px;" />
          </div>
        {/if}
        <div class="form-floating mb-3">
          <input
            type="text"
            class="form-control"
            id="media-title"
            bind:value={title}
            placeholder={$_('modals.edit-media.fields.title')} />
          <label for="media-title">{$_('modals.edit-media.fields.title')}</label>
        </div>
        <div class="form-floating mb-3">
          <textarea
            class="form-control"
            id="media-description"
            style="height: 100px"
            bind:value={description}
            placeholder={$_('modals.edit-media.fields.description')}></textarea>
          <label for="media-description">{$_('modals.edit-media.fields.description')}</label>
        </div>
        <div class="small">
          <i class="fas fa-file me-1"></i>
          {$item.originalFileName}
          <span class="ms-2">({formatFileSize($item.fileSize)})</span>
          {#if $item.width && $item.height}
            <span class="ms-2">{$item.width} × {$item.height}</span>
          {/if}
        </div>
      </div>
      <div class="modal-footer">
        <button
          class="btn btn-secondary w-100"
          type="button"
          class:disabled={loading || !hasChanges}
          on:click={onSave}>
          {#if loading}
            <span class="spinner-border spinner-border-sm me-2" role="status"></span>
          {/if}
          {$_('modals.edit-media.save')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
  import { get, writable } from 'svelte/store';
  import ApiUtil from '@panomc/sdk/utils/api';

  const modalElement = writable();
  const item = writable({});
  let callback = (item) => {};
  let modal;

  export function show(mediaItem) {
    item.set(mediaItem);
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
  import { showToast } from '@panomc/sdk/toasts';
  import { _, pluginId } from '../../../main';

  let loading = false;
  let title = '';
  let description = '';

  $: {
    title = $item.title || '';
    description = $item.description || '';
  }

  $: hasChanges = title !== ($item.title || '') || description !== ($item.description || '');

  function formatFileSize(bytes) {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  async function onSave() {
    loading = true;
    try {
      const result = await ApiUtil.put({
        path: `/api/panel/media-items/${$item.id}`,
        body: { title, description },
      });

      if (result.error) {
        showToast(`plugins.${pluginId}.toasts.error-saving`, { error: result.error });
      } else {
        callback({ ...$item, title, description });
        showToast(`plugins.${pluginId}.toasts.media-updated`);
        hide();
      }
    } catch (e) {
      console.error(e);
      showToast(`plugins.${pluginId}.toasts.error-saving`, { error: 'An error occurred' });
    } finally {
      loading = false;
    }
  }
</script>
