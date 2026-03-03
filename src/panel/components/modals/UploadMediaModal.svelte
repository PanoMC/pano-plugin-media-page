<!-- Upload Media Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered modal-lg" role="dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">{$_('modals.upload-media.title')}</h5>
        <button type="button" class="btn-close" on:click={hide} aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <!-- Optional Title & Description -->
        <div class="row g-3 mb-3">
          <div class="col-12">
            <label for="upload-title" class="form-label">{$_('modals.upload-media.fields.title')}</label>
            <input type="text" class="form-control" id="upload-title" bind:value={title}
              placeholder={$_('modals.upload-media.fields.title-placeholder')} />
          </div>
          <div class="col-12">
            <label for="upload-description" class="form-label">{$_('modals.upload-media.fields.description')}</label>
            <textarea class="form-control" id="upload-description" rows="2" bind:value={description}
              placeholder={$_('modals.upload-media.fields.description-placeholder')}></textarea>
          </div>
        </div>

        <!-- Drag & Drop Area -->
        <div
          class="upload-area"
          class:drag-over={isDragOver}
          on:dragover|preventDefault={() => { isDragOver = true; }}
          on:dragleave={() => { isDragOver = false; }}
          on:drop|preventDefault={onDrop}
          role="button"
          tabindex="0">
          <input
            type="file"
            multiple
            accept="image/*,video/*"
            id="upload-file-input"
            style="display: none"
            on:change={onFileSelect} />
          <label for="upload-file-input" class="upload-label">
            <i class="fas fa-cloud-upload-alt fa-3x mb-2"></i>
            <span>{$_('modals.upload-media.drop-label')}</span>
            <small class="text-muted">{$_('modals.upload-media.drop-hint')}</small>
          </label>
        </div>

        <!-- Selected Files Preview -->
        {#if selectedFiles.length > 0}
          <div class="mt-3">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <small class="text-muted">
                {$_('modals.upload-media.selected-count', { values: { count: selectedFiles.length } })}
              </small>
              <button type="button" class="btn btn-sm btn-link text-danger p-0" on:click={clearFiles}>
                {$_('modals.upload-media.clear')}
              </button>
            </div>
            <div class="file-list">
              {#each selectedFiles as file, i (i)}
                <div class="file-item">
                  <div class="d-flex align-items-center gap-2 overflow-hidden">
                    {#if file.type.startsWith('image/')}
                      <img src={getFilePreviewUrl(file)} alt={file.name} class="file-thumb" />
                    {:else if file.type.startsWith('video/')}
                      <div class="file-thumb video-preview-container">
                        <video src={getFilePreviewUrl(file)} preload="metadata" muted></video>
                        <div class="video-preview-overlay">
                          <i class="fas fa-play"></i>
                        </div>
                      </div>
                    {:else}
                      <div class="file-thumb-icon">
                        <i class="fas fa-file"></i>
                      </div>
                    {/if}
                    <small class="text-truncate">{file.name}</small>
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <small class="text-muted flex-shrink-0">{formatFileSize(file.size)}</small>
                    <button type="button" class="btn btn-sm btn-link p-0 text-danger" on:click={() => removeFile(i)} aria-label="Remove">
                      <i class="fas fa-times"></i>
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          </div>
        {/if}

        <!-- Upload Progress -->
        {#if uploading}
          <div class="progress mt-3">
            <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: {uploadProgress}%">
              {Math.round(uploadProgress)}%
            </div>
          </div>
        {/if}
      </div>
      <div class="modal-footer">
        <button class="btn btn-link" type="button" class:disabled={uploading} on:click={hide}>
          {$_('modals.upload-media.cancel')}
        </button>
        <button class="btn btn-secondary" type="button"
          class:disabled={uploading || selectedFiles.length === 0}
          on:click={onUpload}>
          {#if uploading}
            <span class="spinner-border spinner-border-sm me-2" role="status"></span>
          {/if}
          <i class="fas fa-upload me-1"></i>
          {$_('modals.upload-media.upload')}
        </button>
      </div>
    </div>
  </div>
</div>

<style>
  .upload-area {
    border: 2px dashed var(--bs-border-color);
    border-radius: 8px;
    transition: all 0.2s ease;
  }
  .upload-area:hover,
  .upload-area.drag-over {
    border-color: var(--bs-primary);
    background: var(--bs-primary-bg-subtle);
  }
  .upload-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    cursor: pointer;
    text-align: center;
    opacity: 0.7;
  }
  .upload-label:hover {
    opacity: 1;
  }
  .file-list {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid var(--bs-border-color);
    border-radius: 6px;
  }
  .file-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.4rem 0.75rem;
    gap: 0.5rem;
  }
  .file-item + .file-item {
    border-top: 1px solid var(--bs-border-color);
  }
  .file-thumb {
    width: 36px;
    height: 36px;
    border-radius: 4px;
    object-fit: cover;
    flex-shrink: 0;
  }
  .file-thumb-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bs-dark-bg-subtle);
    border-radius: 4px;
    flex-shrink: 0;
  }
  .video-preview-container {
    position: relative;
    background: #000;
    border-radius: 4px;
    overflow: hidden;
  }
  .video-preview-container video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .video-preview-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.2);
    font-size: 0.6rem;
    color: #fff;
  }
</style>

<script context="module">
  import { get, writable } from 'svelte/store';
  import ApiUtil from '@panomc/sdk/utils/api';

  const modalElement = writable();
  let categoryId = null;
  let callback = (items) => {};
  let modal;

  export function show(newCategoryId) {
    categoryId = newCategoryId;

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

  let uploading = false;
  let uploadProgress = 0;
  let title = '';
  let description = '';
  let selectedFiles = [];
  let isDragOver = false;

  function onFileSelect(event) {
    const files = event.target.files;
    if (files && files.length > 0) {
      selectedFiles = [...selectedFiles, ...Array.from(files)];
    }
    event.target.value = '';
  }

  function onDrop(event) {
    isDragOver = false;
    const files = event.dataTransfer.files;
    if (files && files.length > 0) {
      selectedFiles = [...selectedFiles, ...Array.from(files)];
    }
  }

  function clearFiles() {
    Object.values(previewUrls).forEach((url) => URL.revokeObjectURL(url));
    previewUrls = {};
    selectedFiles = [];
  }

  function removeFile(index) {
    if (previewUrls[index]) {
      URL.revokeObjectURL(previewUrls[index]);
      delete previewUrls[index];
    }
    selectedFiles = selectedFiles.filter((_, i) => i !== index);
    // Re-index preview URLs
    const newUrls = {};
    selectedFiles.forEach((file, i) => {
      if (file.type.startsWith('image/') || file.type.startsWith('video/')) {
        newUrls[i] = URL.createObjectURL(file);
      }
    });
    previewUrls = newUrls;
  }

  let previewUrls = {};

  function getFilePreviewUrl(file) {
    const index = selectedFiles.indexOf(file);
    if (!previewUrls[index]) {
      if (file.type.startsWith('image/') || file.type.startsWith('video/')) {
        previewUrls[index] = URL.createObjectURL(file);
      }
    }
    return previewUrls[index];
  }

  function formatFileSize(bytes) {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  async function onUpload() {
    if (selectedFiles.length === 0) return;

    uploading = true;
    uploadProgress = 0;

    try {
      const formData = new FormData();
      for (let i = 0; i < selectedFiles.length; i++) {
        formData.append('file', selectedFiles[i]);
      }

      if (title.trim()) {
        formData.append('title', title.trim());
      }
      if (description.trim()) {
        formData.append('description', description.trim());
      }

      const result = await ApiUtil.post({
        path: `/api/panel/media-items/upload/${categoryId}`,
        body: formData,
        onUploadProgress: (progress) => {
          uploadProgress = progress * 100;
        },
      });

      if (result && result.error) {
        showToast(`plugins.${pluginId}.toasts.error-uploading`, { error: result.error });
      } else {
        const responseData = result?.data || result || {};
        const newItems = responseData.items || [];

        callback({ categoryId, items: newItems });
        showToast(`plugins.${pluginId}.toasts.media-uploaded`);

        // Reset form
        title = '';
        description = '';
        selectedFiles = [];
        uploadProgress = 0;
        hide();
      }
    } catch (e) {
      console.error(e);
      showToast(`plugins.${pluginId}.toasts.error-uploading`, {
        error: $_(`plugins.${pluginId}.errors.ERROR_GENERAL`),
      });
    } finally {
      uploading = false;
    }
  }
</script>
