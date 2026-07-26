<style>
  .media-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 1rem;
  }
  .card.dragging {
    opacity: 0.5;
  }
  .media-card {
    border: 1px solid var(--bs-border-color);
    border-radius: 8px;
    overflow: hidden;
    transition:
      box-shadow 0.2s ease,
      border-color 0.2s ease;
    cursor: grab;
  }
  .media-card.dragging {
    opacity: 0.5;
  }
  .media-card:active {
    cursor: grabbing;
  }
  .media-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  .media-preview {
    width: 100%;
    height: 120px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bs-body-bg);
    position: relative;
  }
  .media-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .video-preview {
    position: relative;
    width: 100%;
    height: 100%;
    background: #000;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .video-preview video,
  .video-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .video-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.3);
    color: #fff;
    opacity: 0.8;
  }
  .media-info {
    padding: 0.5rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.25rem;
  }
  .media-actions {
    display: flex;
    gap: 0.25rem;
    flex-shrink: 0;
  }
  .drag-handle {
    cursor: grab;
    padding: 0.25rem;
  }
  .drag-handle:active {
    cursor: grabbing;
  }
</style>

<article class="container vstack gap-3">
  <PageActions>
    <div slot="left">
      <button type="button" class="btn btn-link text-decoration-none" on:click={goBack}>
        <i class="fas fa-arrow-left me-2"></i>
        {$_('media-pages.editor.back')}
      </button>
    </div>
    <div slot="right">
      <button
        type="button"
        class="btn btn-secondary"
        disabled={saving || !hasChanges || !isValid}
        on:click={save}>
        {#if saving}
          <span class="spinner-border spinner-border-sm me-2" role="status"></span>
        {/if}
        {$_('media-pages.editor.save')}
      </button>
    </div>
  </PageActions>

  <!-- Page Settings -->
  <div class="card">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-6">
          <label for="title" class="form-label">{$_('media-pages.editor.fields.title')}</label>
          <input
            type="text"
            class="form-control"
            id="title"
            bind:value={form.title}
            on:input={onTitleInput} />
        </div>
        <div class="col-md-6">
          <label for="slug" class="form-label">{$_('media-pages.editor.fields.slug')}</label>
          <input type="text" class="form-control" id="slug" bind:value={form.slug} />
        </div>
        <div class="col-12">
          <label for="description" class="form-label"
            >{$_('media-pages.editor.fields.description')}</label>
          <textarea class="form-control" id="description" rows="3" bind:value={form.description}
          ></textarea>
        </div>
        <div class="col-md-6">
          <div class="form-check form-switch">
            <input
              class="form-check-input"
              type="checkbox"
              id="active"
              bind:checked={form.active} />
            <label class="form-check-label" for="active"
              >{$_('media-pages.editor.fields.active')}</label>
          </div>
        </div>
        <div class="col-md-6">
          <div class="form-check form-switch">
            <input
              class="form-check-input"
              type="checkbox"
              id="registerToThemeNav"
              bind:checked={form.registerToThemeNav} />
            <label class="form-check-label" for="registerToThemeNav"
              >{$_('media-pages.editor.fields.register-to-nav')}</label>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Categories section -->
  <div class="d-flex align-items-center justify-content-between">
    <h5 class="mb-0">{$_('media-pages.editor.categories.title')}</h5>
    <button type="button" class="btn btn-primary" on:click={showAddCategory}>
      <i class="fas fa-plus me-1"></i>
      {$_('media-pages.editor.categories.add')}
    </button>
  </div>

  <!-- Edit mode: full category & media management -->
  {#if categories.length === 0}
    <div class="card">
      <NoContent />
    </div>
  {:else}
    {#each categories as cat, catIndex (cat.category.id)}
      <div
        class="card {draggingCategoryIndex === catIndex ? 'dragging' : ''}"
        id="category-card-{cat.category.id}"
        animate:flip={{ duration: 250 }}
        on:dragover|preventDefault={(e) => onCategoryDragOver(e, catIndex)}
        on:dragleave={(e) => onCategoryDragLeave(e, catIndex)}
        on:drop|preventDefault={(e) => onCategoryDrop(e, catIndex)}>
        <div class="card-header d-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center gap-2">
            <!-- Drag handle for category reorder -->
            <div
              class="drag-handle d-flex flex-column gap-1"
              draggable="true"
              on:dragstart={(e) => onCategoryDragStart(e, catIndex, cat.category.id)}
              on:dragend={onCategoryDragEnd}
              role="button"
              tabindex="0"
              on:keydown={(e) => {
                if (e.key === 'ArrowUp' && catIndex > 0) swapCategories(catIndex, catIndex - 1);
                if (e.key === 'ArrowDown' && catIndex < categories.length - 1)
                  swapCategories(catIndex, catIndex + 1);
              }}
              aria-label="Drag to reorder">
              <i class="fas fa-grip-vertical text-muted"></i>
            </div>
            <i class="fas fa-layer-group text-muted"></i>
            <strong>{cat.category.title}</strong>
            {#if cat.category.description}
              <small class="text-muted ms-2 d-none d-md-inline">— {cat.category.description}</small>
            {/if}
            {#if !cat.category.visible}
              <span class="badge bg-danger ms-1">{$_('media-pages.editor.categories.hidden')}</span>
            {/if}
          </div>
          <div class="d-flex align-items-center gap-2">
            <!-- Visibility toggle -->
            <div class="form-check form-switch mb-0">
              <input
                class="form-check-input"
                type="checkbox"
                checked={cat.category.visible}
                on:change={() => toggleCategoryVisibility(cat.category)}
                id="visible-{cat.category.id}"
                title={$_('media-pages.editor.categories.toggle-visibility')}
                aria-label={$_('media-pages.editor.categories.toggle-visibility')} />
            </div>
            {#if isEditing}
              <!-- Add Media -->
              <button
                type="button"
                class="btn btn-sm btn-link text-decoration-none"
                on:click={() => openUploadModal(cat.category.id)}
                title={$_('media-pages.editor.upload.add-media')}
                aria-label={$_('media-pages.editor.upload.add-media')}>
                <i class="fas fa-upload"></i>
              </button>
              <!-- Move to another page -->
              <button
                type="button"
                class="btn btn-sm btn-link text-decoration-none"
                on:click={() => moveCategory(cat.category)}
                title={$_('media-pages.editor.categories.move')}
                aria-label={$_('media-pages.editor.categories.move')}>
                <i class="fas fa-arrow-right-arrow-left"></i>
              </button>
            {/if}
            <!-- Edit -->
            <button
              type="button"
              class="btn btn-sm btn-link text-decoration-none"
              on:click={() => editCategory(cat.category)}
              title={$_('media-pages.list.table.actions-edit')}
              aria-label={$_('media-pages.list.table.actions-edit')}>
              <i class="fas fa-edit"></i>
            </button>
            <!-- Delete -->
            <button
              type="button"
              class="btn btn-sm btn-link text-danger text-decoration-none"
              on:click={() => deleteCategory(cat.category)}
              title={$_('media-pages.list.table.actions-delete')}
              aria-label={$_('media-pages.list.table.actions-delete')}>
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>
        <div class="card-body">
          <!-- Media Items Grid -->
          {#if !isEditing}
            <div class="text-center text-muted py-4">
              <i class="fas fa-save fa-2x mb-2 opacity-50"></i>
              <p class="mb-0">
                {$_('media-pages.editor.categories.save-first-hint') !==
                'media-pages.editor.categories.save-first-hint'
                  ? $_('media-pages.editor.categories.save-first-hint')
                  : 'Medya yüklemek için önce sayfayı kaydedin.'}
              </p>
            </div>
          {:else if cat.items && cat.items.length > 0}
            <div class="media-grid">
              {#each cat.items as item, itemIndex (item.id)}
                <div
                  class="media-card {draggingMedia &&
                  draggingMedia.categoryId === cat.category.id &&
                  draggingMedia.itemIndex === itemIndex
                    ? 'dragging'
                    : ''}"
                  animate:flip={{ duration: 150 }}
                  draggable="true"
                  on:dragstart={(e) => onMediaDragStart(e, cat.category.id, itemIndex)}
                  on:dragover|preventDefault={(e) => onMediaDragOver(e, cat.category.id, itemIndex)}
                  on:dragleave={(e) => onMediaDragLeave(e, cat.category.id, itemIndex)}
                  on:drop|preventDefault={(e) => onMediaDrop(e, cat.category.id, itemIndex)}
                  on:dragend={onMediaDragEnd}
                  role="button"
                  tabindex="0"
                  aria-label="Drag to reorder media"
                  on:keydown={(e) => {}}>
                  <div class="media-preview">
                    {#if item.type === 'IMAGE'}
                      <img
                        src="/api/media/file/{item.thumbnailPath || item.filePath}"
                        alt={item.title}
                        loading="lazy" />
                    {:else}
                      <div class="video-preview">
                        {#if item.thumbnailPath}
                          <img
                            src="/api/media/file/{item.thumbnailPath}"
                            alt={item.title}
                            loading="lazy" />
                        {:else}
                          <video
                            src="/api/media/file/{item.filePath}#t=0.1"
                            preload="metadata"
                            muted></video>
                        {/if}
                        <div class="video-overlay">
                          <i class="fas fa-play-circle fa-2x"></i>
                        </div>
                        <span class="badge bg-dark position-absolute bottom-0 end-0 m-1">
                          <i class="fas fa-video me-1"></i>Video
                        </span>
                      </div>
                    {/if}
                  </div>
                  <div class="media-info">
                    <small class="text-truncate d-block" title={item.title}>{item.title}</small>
                    <div class="media-actions">
                      <button
                        type="button"
                        class="btn btn-sm btn-link p-0"
                        on:click|stopPropagation={() => editMedia(item)}
                        title={$_('media-pages.editor.media.edit')}
                        aria-label={$_('media-pages.editor.media.edit')}>
                        <i class="fas fa-edit"></i>
                      </button>
                      <button
                        type="button"
                        class="btn btn-sm btn-link p-0 text-danger"
                        on:click|stopPropagation={() => deleteMedia(item)}
                        title={$_('media-pages.editor.media.delete')}
                        aria-label={$_('media-pages.editor.media.delete')}>
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          {:else}
            <NoContent />
          {/if}
        </div>
      </div>
    {/each}
  {/if}

  <AddCategoryModal />
  <EditCategoryModal />
  <ConfirmDeleteCategoryModal />
  <UploadMediaModal />
  <EditMediaModal />
  <ConfirmDeleteMediaModal />
  <MoveCategoryModal />
</article>

<script context="module">
  import ApiUtil from '@panomc/sdk/utils/api';
  import { error } from '@panomc/sdk/svelte';
  import { pluginId } from '../../main';

  export async function load(event) {
    const { parent, params } = event;
    const { pageTitle } = await parent();

    const isEditing = !!params.id;

    if (isEditing) {
      pageTitle.set(`plugins.${pluginId}.media-pages.editor.edit-title`);

      const body = await ApiUtil.get({
        path: `/api/panel/media-pages/${params.id}`,
        request: event,
      });

      if (body.error) {
        throw error(404);
      }

      const data = body.data || body;
      return {
        data: {
          isEditing: true,
          mediaPage: data.mediaPage,
          categories: data.categories || [],
        },
      };
    } else {
      pageTitle.set(`plugins.${pluginId}.media-pages.editor.create-title`);
      return {
        data: {
          isEditing: false,
          mediaPage: null,
          categories: [],
        },
      };
    }
  }
</script>

<script>
  import { flip } from 'svelte/animate';
  import { base, goto } from '@panomc/sdk/svelte';
  import { PageActions, NoContent } from '@panomc/sdk/components/panel';
  import { _, showSuccessToast, showErrorToast } from '../../main';

  import AddCategoryModal, {
    show as showAddCategoryModal,
    setCallback as setAddCategoryCallback,
  } from '../components/modals/AddCategoryModal.svelte';

  import EditCategoryModal, {
    show as showEditCategoryModal,
    setCallback as setEditCategoryCallback,
  } from '../components/modals/EditCategoryModal.svelte';

  import ConfirmDeleteCategoryModal, {
    show as showDeleteCategoryModal,
    setCallback as setDeleteCategoryCallback,
  } from '../components/modals/ConfirmDeleteCategoryModal.svelte';

  import EditMediaModal, {
    show as showEditMediaModal,
    setCallback as setEditMediaCallback,
  } from '../components/modals/EditMediaModal.svelte';

  import UploadMediaModal, {
    show as showUploadMediaModal,
    setCallback as setUploadMediaCallback,
  } from '../components/modals/UploadMediaModal.svelte';

  import ConfirmDeleteMediaModal, {
    show as showDeleteMediaModal,
    setCallback as setDeleteMediaCallback,
  } from '../components/modals/ConfirmDeleteMediaModal.svelte';

  import MoveCategoryModal, {
    show as showMoveCategoryModal,
    setCallback as setMoveCategoryCallback,
  } from '../components/modals/MoveCategoryModal.svelte';

  export let data;

  let saving = false;
  let draggingCategoryIndex = null;
  let draggingMedia = null;
  let categoryOrderChanged = false;
  let modifiedMediaCategories = new Set();
  let initialCategoryOrder = [];
  let initialMediaOrders = {}; // categoryId -> [itemIds]

  let isEditing = data.isEditing;
  let categories = data.categories || [];
  let form = {
    title: data.mediaPage?.title || '',
    slug: data.mediaPage?.slug || '',
    description: data.mediaPage?.description || '',
    active: data.mediaPage?.active ?? true,
    registerToThemeNav: data.mediaPage?.registerToThemeNav ?? true,
  };

  let initialFormStr = JSON.stringify(form);
  let initialCategoriesCount = data.categories ? data.categories.length : 0;
  $: hasChanges =
    JSON.stringify(form) !== initialFormStr ||
    (!isEditing && categories.length > initialCategoriesCount);
  $: isValid = form.title.trim() !== '' && form.slug.trim() !== '';

  let oldTitle = form.title || '';

  function slugify(text) {
    if (!text) return '';
    return text
      .toLowerCase()
      .replace(/[^a-z0-9\s-]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .trim();
  }

  function onTitleInput() {
    const generatedSlug = slugify(form.title);
    const oldGeneratedSlug = slugify(oldTitle);

    if (!form.slug || form.slug === oldGeneratedSlug) {
      form.slug = generatedSlug;
    }

    oldTitle = form.title;
  }

  function goBack() {
    goto(`${base}/media-pages`);
  }

  async function save() {
    saving = true;
    try {
      if (isEditing) {
        const result = await ApiUtil.put({
          path: `/api/panel/media-pages/${data.mediaPage.id}`,
          body: form,
        });

        if (result.error) {
          const errorKey = `plugins.${pluginId}.errors.${result.error}`;
          const translatedError = $_(errorKey) !== errorKey ? $_(errorKey) : result.error;
          showErrorToast(`plugins.${pluginId}.toasts.error-saving`, { error: translatedError });
        } else {
          showSuccessToast(`plugins.${pluginId}.toasts.page-updated`);
          initialFormStr = JSON.stringify(form);
        }
      } else {
        const postBody = { ...form };
        if (categories.length > 0) {
          postBody.categories = categories.map((c) => ({
            title: c.category.title?.trim() || '',
            description: c.category.description?.trim() || '',
            visible: c.category.visible !== false,
          }));
        }

        const result = await ApiUtil.post({
          path: '/api/panel/media-pages',
          body: postBody,
        });

        if (result.error) {
          const errorKey = `plugins.${pluginId}.errors.${result.error}`;
          const translatedError = $_(errorKey) !== errorKey ? $_(errorKey) : result.error;
          showErrorToast(`plugins.${pluginId}.toasts.error-saving`, { error: translatedError });
        } else {
          showSuccessToast(`plugins.${pluginId}.toasts.page-added`);
          const responseData = result.data || result;
          goto(`${base}/media-pages/edit/${responseData.id}`);
        }
      }
    } catch (e) {
      console.error(e);
      showErrorToast(`plugins.${pluginId}.toasts.error-saving`, {
        error: $_(`plugins.${pluginId}.errors.ERROR_GENERAL`),
      });
    } finally {
      saving = false;
    }
  }

  // Category management
  function showAddCategory() {
    showAddCategoryModal(data.mediaPage ? data.mediaPage.id : null);
  }

  setAddCategoryCallback((newCategory) => {
    categories = [...categories, { category: newCategory, items: [] }];
  });

  function editCategory(category) {
    showEditCategoryModal(category);
  }

  setEditCategoryCallback((updatedCategory) => {
    categories = categories.map((c) =>
      c.category.id === updatedCategory.id ? { ...c, category: updatedCategory } : c,
    );
  });

  function deleteCategory(category) {
    if (typeof category.id === 'string' && category.id.startsWith('temp_')) {
      categories = categories.filter((c) => c.category.id !== category.id);
      return;
    }
    showDeleteCategoryModal(category);
  }

  setDeleteCategoryCallback((deletedCategory) => {
    categories = categories.filter((c) => c.category.id !== deletedCategory.id);
  });

  // Category visibility toggle
  async function toggleCategoryVisibility(category) {
    if (typeof category.id === 'string' && category.id.startsWith('temp_')) {
      categories = categories.map((c) =>
        c.category.id === category.id
          ? { ...c, category: { ...c.category, visible: !c.category.visible } }
          : c,
      );
      return;
    }
    try {
      const result = await ApiUtil.put({
        path: `/api/panel/media-categories/${category.id}/visibility`,
      });

      if (result && !result.error) {
        const newVisible = result.visible ?? result.data?.visible ?? !category.visible;
        categories = categories.map((c) =>
          c.category.id === category.id
            ? { ...c, category: { ...c.category, visible: newVisible } }
            : c,
        );
        showSuccessToast(`plugins.${pluginId}.toasts.category-updated`);
      } else if (result?.error) {
        const errorKey = `plugins.${pluginId}.errors.${result.error}`;
        const translatedError = $_(errorKey) !== errorKey ? $_(errorKey) : result.error;
        showErrorToast(`plugins.${pluginId}.toasts.error-saving`, { error: translatedError });
      }
    } catch (e) {
      console.error(e);
    }
  }

  // Category drag & drop reorder
  function onCategoryDragStart(event, index, categoryId) {
    const dragIndex = index;
    initialCategoryOrder = categories.map((c) => c.category.id);
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', 'category');

    const el = document.getElementById(`category-card-${categoryId}`);
    if (el) {
      event.dataTransfer.setDragImage(el, event.clientX - el.getBoundingClientRect().left, 20);
    }

    // Delay applying the dragging index to ensure browser captures full-opacity drag image
    setTimeout(() => {
      draggingCategoryIndex = dragIndex;
    }, 20);
  }

  function onCategoryDragOver(event, index) {
    if (draggingCategoryIndex !== null && draggingCategoryIndex !== index) {
      const rect = event.currentTarget.getBoundingClientRect();
      const midY = rect.top + rect.height / 2;

      // Yalnızca hedefin yarısını (merkezini) geçtiğinde yer değiştir, böylece titreme olmaz
      if (draggingCategoryIndex < index && event.clientY < midY) return;
      if (draggingCategoryIndex > index && event.clientY > midY) return;

      const newCategories = [...categories];
      const [moved] = newCategories.splice(draggingCategoryIndex, 1);
      newCategories.splice(index, 0, moved);
      categories = newCategories;
      draggingCategoryIndex = index;
      categoryOrderChanged = true;
    }
  }

  function onCategoryDragLeave(event, index) {}

  function onCategoryDrop(event, targetIndex) {
    // Drop işlevini boş bırakıyoruz çünkü dragend daha güvenilir (live-sort sırasında drop tetiklenmeyebilir)
  }

  function onCategoryDragEnd() {
    draggingCategoryIndex = null;
    const currentOrder = categories.map((c) => c.category.id);
    const hasChanged = JSON.stringify(initialCategoryOrder) !== JSON.stringify(currentOrder);

    if (hasChanged) {
      saveCategoryOrder();
    }
    categoryOrderChanged = false;
  }

  function swapCategories(fromIndex, toIndex) {
    const newCategories = [...categories];
    const [moved] = newCategories.splice(fromIndex, 1);
    newCategories.splice(toIndex, 0, moved);
    categories = newCategories;
    saveCategoryOrder();
  }

  async function saveCategoryOrder() {
    if (!isEditing) return;
    try {
      const result = await ApiUtil.put({
        path: '/api/panel/media-categories/reorder',
        body: {
          categories: categories.map((c, i) => ({
            id: c.category.id,
            sortOrder: i,
          })),
        },
      });
      if (result && !result.error) {
        showSuccessToast(`plugins.${pluginId}.toasts.category-updated`);
      } else if (result?.error) {
        const errorKey = `plugins.${pluginId}.errors.${result.error}`;
        const translatedError = $_(errorKey) !== errorKey ? $_(errorKey) : result.error;
        showErrorToast(`plugins.${pluginId}.toasts.error-saving`, { error: translatedError });
      }
    } catch (e) {
      console.error('Failed to save category order', e);
    }
  }

  // Move category to another page
  function moveCategory(category) {
    showMoveCategoryModal(category, data.mediaPage.id);
  }

  setMoveCategoryCallback((movedCategory) => {
    categories = categories.filter((c) => c.category.id !== movedCategory.id);
    showSuccessToast(`plugins.${pluginId}.toasts.category-moved`);
  });

  // Media upload via modal
  function openUploadModal(categoryId) {
    showUploadMediaModal(categoryId);
  }

  setUploadMediaCallback(({ categoryId: catId, items: newItems }) => {
    categories = categories.map((c) => {
      if (c.category.id === catId) {
        return { ...c, items: [...c.items, ...newItems] };
      }
      return c;
    });
  });

  // Media drag & drop reorder
  function onMediaDragStart(event, categoryId, itemIndex) {
    const dragData = { categoryId, itemIndex };
    const target = event.currentTarget;

    // Store initial order for this category
    const cat = categories.find((c) => c.category.id === categoryId);
    if (cat) {
      initialMediaOrders[categoryId] = cat.items.map((i) => i.id);
    }

    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', 'media');

    if (target) {
      const rect = target.getBoundingClientRect();
      // Set drag image with mouse offset for better "in-hand" feel
      event.dataTransfer.setDragImage(target, event.clientX - rect.left, event.clientY - rect.top);
    }

    // Delay setting draggingMedia to ensure a clean drag image capture with full opacity
    setTimeout(() => {
      draggingMedia = dragData;
    }, 20);
  }

  let lastMediaSwapTime = 0;

  function onMediaDragOver(event, categoryId, itemIndex) {
    if (
      draggingMedia &&
      draggingMedia.categoryId === categoryId &&
      draggingMedia.itemIndex !== itemIndex
    ) {
      const now = Date.now();
      if (now - lastMediaSwapTime < 50) return; // Prevent rapid-fire swaps during animations

      const rect = event.currentTarget.getBoundingClientRect();
      const relX = (event.clientX - rect.left) / rect.width;
      const relY = (event.clientY - rect.top) / rect.height;

      const fromIndex = draggingMedia.itemIndex;
      const toIndex = itemIndex;

      // Hysteresis logic (60/40 split) to prevent "right-left" bug (oscillation):
      // Pointer must be deeply in the target's half matching the drag direction.
      if (fromIndex < toIndex) {
        // Moving forward (Right / Down): must cross 60% of target
        if (relX < 0.6 && relY < 0.6) return;
      } else {
        // Moving backward (Left / Up): must be before 40% of target
        if (relX > 0.4 && relY > 0.4) return;
      }

      const cat = categories.find((c) => c.category.id === categoryId);
      if (!cat) return;

      const newItems = [...cat.items];

      // Swap items
      const [moved] = newItems.splice(fromIndex, 1);
      newItems.splice(toIndex, 0, moved);

      // Trigger Svelte reactivity
      categories = categories.map((c) =>
        c.category.id === categoryId ? { ...c, items: newItems } : c,
      );

      // Update dragging state and timing
      draggingMedia = { ...draggingMedia, itemIndex: toIndex };
      lastMediaSwapTime = now;
      modifiedMediaCategories.add(categoryId);
    }
  }

  function onMediaDragLeave(event, categoryId, itemIndex) {}

  function onMediaDrop(event, categoryId, targetIndex) {
    // Drop işlevini boş bırakıyoruz çünkü dragend daha güvenilir
  }

  function onMediaDragEnd() {
    draggingMedia = null;
    modifiedMediaCategories.forEach((catId) => {
      const cat = categories.find((c) => c.category.id === catId);
      if (cat) {
        const currentOrder = cat.items.map((i) => i.id);
        const hasChanged =
          JSON.stringify(initialMediaOrders[catId]) !== JSON.stringify(currentOrder);
        if (hasChanged) {
          saveMediaOrder(catId, cat.items);
        }
      }
    });
    modifiedMediaCategories.clear();
    initialMediaOrders = {};
  }

  async function saveMediaOrder(categoryId, items) {
    try {
      const result = await ApiUtil.put({
        path: '/api/panel/media-items/reorder',
        body: {
          items: items.map((item, i) => ({
            id: item.id,
            sortOrder: i,
          })),
        },
      });
      if (result && !result.error) {
        showSuccessToast(`plugins.${pluginId}.toasts.media-updated`);
      } else if (result?.error) {
        const errorKey = `plugins.${pluginId}.errors.${result.error}`;
        const translatedError = $_(errorKey) !== errorKey ? $_(errorKey) : result.error;
        showErrorToast(`plugins.${pluginId}.toasts.error-saving`, { error: translatedError });
      }
    } catch (e) {
      console.error('Failed to save media order', e);
    }
  }

  function editMedia(item) {
    showEditMediaModal(item);
  }

  setEditMediaCallback((updatedItem) => {
    categories = categories.map((c) => ({
      ...c,
      items: c.items.map((i) => (i.id === updatedItem.id ? { ...i, ...updatedItem } : i)),
    }));
  });

  function deleteMedia(item) {
    showDeleteMediaModal(item);
  }

  setDeleteMediaCallback((deletedItem) => {
    categories = categories.map((c) => ({
      ...c,
      items: c.items.filter((i) => i.id !== deletedItem.id),
    }));
  });
</script>
