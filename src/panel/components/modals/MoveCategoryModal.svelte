<style>
  .text-first-capitalize::first-letter {
    text-transform: uppercase;
  }
</style>

<!-- Move Category Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered" role="dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">{$_('modals.move-category.title')}</h5>
        <button type="button" class="btn-close" on:click={hide} aria-label="Close"></button>
      </div>
      <div class="modal-body text-first-capitalize">
        <p>
          {@html $_('modals.move-category.description', {
            values: { title: `<span class="badge text-bg-gray">${$category.title}</span>` },
          })}
        </p>
        {#if loading}
          <div class="text-center py-3">
            <span class="spinner-border spinner-border-sm" role="status"></span>
          </div>
        {:else if pages.length === 0}
          <div class="text-muted text-center py-3">
            {$_('modals.move-category.no-pages')}
          </div>
        {:else}
          <select class="form-select" bind:value={selectedPageId}>
            <option value={null} disabled>{$_('modals.move-category.select-page')}</option>
            {#each pages as page (page.id)}
              <option value={page.id}>{page.title}</option>
            {/each}
          </select>
        {/if}
      </div>
      <div class="modal-footer">
        <button
          class="btn btn-secondary w-100"
          type="button"
          class:disabled={moving || !selectedPageId}
          on:click={onMove}>
          {#if moving}
            <span class="spinner-border spinner-border-sm me-2" role="status"></span>
          {/if}
          {$_('modals.move-category.move')}
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
  let currentPageId = null;
  let callback = (category) => {};
  let modal;
  let onShowCallback = null;

  export function show(cat, pageId) {
    category.set(cat);
    currentPageId = pageId;
    modal = new window.bootstrap.Modal(get(modalElement), {
      backdrop: 'static',
      keyboard: false,
    });
    modal.show();
    if (onShowCallback) onShowCallback();
  }

  export function setCallback(newCallback) {
    callback = newCallback;
  }

  export function hide() {
    modal.hide();
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { showToast } from '@panomc/sdk/toasts';
  import { _, pluginId } from '../../../main';

  let loading = false;
  let moving = false;
  let pages = [];
  let selectedPageId = null;

  onShowCallback = loadPages;

  async function loadPages() {
    loading = true;
    selectedPageId = null;
    try {
      const result = await ApiUtil.get({
        path: '/api/panel/media-pages',
      });
      const allPages = result?.data?.mediaPages || result?.mediaPages || [];
      // Filter out current page
      pages = allPages.filter((p) => p.id !== currentPageId);
    } catch (e) {
      console.error(e);
      pages = [];
    } finally {
      loading = false;
    }
  }

  async function onMove() {
    if (!selectedPageId) return;
    moving = true;
    try {
      const result = await ApiUtil.put({
        path: `/api/panel/media-categories/${$category.id}/move`,
        body: { targetPageId: selectedPageId },
      });

      if (result && !result.error) {
        callback($category);
        showToast(`plugins.${pluginId}.toasts.category-moved`);
        hide();
      } else {
        showToast(`plugins.${pluginId}.toasts.error-saving`, {
          error: result?.error || 'Unknown error',
        });
      }
    } catch (e) {
      console.error(e);
      showToast(`plugins.${pluginId}.toasts.error-saving`, { error: 'An error occurred' });
    } finally {
      moving = false;
    }
  }
</script>
