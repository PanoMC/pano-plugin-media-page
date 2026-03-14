<!-- Edit Category Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered" role="dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">{$_('modals.edit-category.title')}</h5>
        <button type="button" class="btn-close" on:click={hide} aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label for="edit-cat-title" class="form-label">{$_('modals.edit-category.fields.title')}</label>
          <input type="text" class="form-control" id="edit-cat-title" bind:value={title} />
        </div>
        <label for="edit-cat-description" class="form-label">{$_('modals.edit-category.fields.description')}</label>
        <textarea class="form-control" id="edit-cat-description" rows="2" bind:value={description}></textarea>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary w-100" type="button" class:disabled={loading || !title.trim() || !hasChanges} on:click={onSave}>
          {#if loading}
            <span class="spinner-border spinner-border-sm me-2" role="status"></span>
          {/if}
          {$_('modals.edit-category.save')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
  import { get, writable } from 'svelte/store';
  import ApiUtil from '@panomc/sdk/utils/api';

  const modalElement = writable();
  const category = writable({});
  let callback = (category) => {};
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
  import { showToast } from '@panomc/sdk/toasts';
  import { _, pluginId } from '../../../main';

  let loading = false;
  let title = '';
  let description = '';

  $: {
    title = $category.title || '';
    description = $category.description || '';
  }

  $: hasChanges = title !== ($category.title || '') || description !== ($category.description || '');

  async function onSave() {
    loading = true;
    try {
      if (typeof $category.id === 'string' && $category.id.startsWith('temp_')) {
        callback({ ...$category, title, description });
        hide();
        return;
      }

      const result = await ApiUtil.put({
        path: `/api/panel/media-categories/${$category.id}`,
        body: { title, description },
      });

      if (result.error) {
        showToast(`plugins.${pluginId}.toasts.error-saving`, { error: result.error });
      } else {
        callback({ ...$category, title, description });
        showToast(`plugins.${pluginId}.toasts.category-updated`);
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
