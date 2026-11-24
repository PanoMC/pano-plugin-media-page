{#if data.categories && data.categories.length > 0}
  {#each data.categories as cat (cat.category.id)}
    <section class="media-page-gallery-page media-page-gallery-page__section">
      <div class="media-page-gallery-page__header">
        <h2 class="media-page-gallery-page__title h4">{cat.category.title}</h2>
        {#if cat.category.description}
          <p class="opacity-75 small mb-0">{cat.category.description}</p>
        {/if}
        <hr class="mt-2 mb-4 opacity-10" />
      </div>

      {#if cat.items && cat.items.length > 0}
        <div class="media-page-gallery-page__grid">
          {#each cat.items as item (item.id)}
            <div class="media-page-gallery-page__item" on:click={() => openLightbox(item)} on:keydown={(e) => { if (e.key === 'Enter') openLightbox(item); }} role="button" tabindex="0">
              <div class="media-page-gallery-page__item-inner">
                {#if item.type === 'IMAGE'}
                  <img
                    src="/api/plugins/pano-plugin-media-page/media/file/{item.thumbnailPath || item.filePath}"
                    alt={item.title}
                    loading="lazy"
                    class="media-page-gallery-page__image" />
                {:else}
                  <div class="media-page-gallery-page__video-thumb">
                    {#if item.thumbnailPath}
                      <img src="/api/plugins/pano-plugin-media-page/media/file/{item.thumbnailPath}" alt={item.title} loading="lazy" class="media-page-gallery-page__image" />
                    {:else}
                      <video src="/api/plugins/pano-plugin-media-page/media/file/{item.filePath}#t=0.1" preload="metadata" muted></video>
                    {/if}
                    <div class="media-page-gallery-page__play-icon">
                      <i class="fas fa-play"></i>
                    </div>
                    <span class="media-page-gallery-page__badge badge position-absolute bottom-0 end-0 m-2">
                      <i class="fas fa-video me-1"></i>{$_('gallery.video')}
                    </span>
                  </div>
                {/if}
                <div class="media-page-gallery-page__overlay">
                  <div class="media-page-gallery-page__overlay-content">
                    {#if item.title}
                      <span class="media-page-gallery-page__item-title d-block">{item.title}</span>
                    {/if}
                    <div class="media-page-gallery-page__zoom-icon mt-2">
                      <i class="fas fa-search-plus"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          {/each}
        </div>
      {:else}
        <div class="py-4 border rounded-3 text-center bg-light bg-opacity-10 text-muted">
          <NoContent />
        </div>
      {/if}
    </section>
  {/each}
{:else}
  <div class="media-page-gallery-page py-5">
    <NoContent />
  </div>
{/if}

<!-- Lightbox -->
{#if lightboxItem}
  <div class="media-page-gallery-page media-page-gallery-page__lightbox" on:click={closeLightbox} transition:fade={{ duration: 200 }}>
    <button type="button" class="media-page-gallery-page__lightbox-nav media-page-gallery-page__nav-prev" on:click|stopPropagation={prevMedia} aria-label={$_('gallery.previous')}>
      <i class="fas fa-chevron-left"></i>
    </button>
    
    <button type="button" class="media-page-gallery-page__lightbox-nav media-page-gallery-page__nav-next" on:click|stopPropagation={nextMedia} aria-label={$_('gallery.next')}>
      <i class="fas fa-chevron-right"></i>
    </button>

    <button type="button" class="media-page-gallery-page__lightbox-close" on:click={closeLightbox} aria-label={$_('gallery.close')}>
      <i class="fas fa-times"></i>
    </button>

    <div class="media-page-gallery-page__lightbox-main" on:click|stopPropagation role="dialog">
      <div class="media-page-gallery-page__media-container">
        {#key lightboxItem.id}
          <div in:fade={{ duration: 250, delay: 50 }} out:fade={{ duration: 150 }} class="media-page-gallery-page__media-wrapper">
            {#if lightboxItem.type === 'IMAGE'}
              <img src="/api/plugins/pano-plugin-media-page/media/file/{lightboxItem.filePath}" alt={lightboxItem.title} class="media-page-gallery-page__lightbox-media" />
            {:else}
              <video src="/api/plugins/pano-plugin-media-page/media/file/{lightboxItem.filePath}" controls autoplay class="media-page-gallery-page__lightbox-media">
                <track kind="captions" />
              </video>
            {/if}
          </div>
        {/key}
      </div>

      {#if lightboxItem.title || lightboxItem.description}
        <div class="media-page-gallery-page__lightbox-details">
          <div class="media-page-gallery-page__details-inner">
            {#if lightboxItem.title}
              <h4 class="media-page-gallery-page__title-2 mb-1 fw-bold">{lightboxItem.title}</h4>
            {/if}
            {#if lightboxItem.description}
              <p class="mb-0 opacity-75">{lightboxItem.description}</p>
            {/if}
          </div>
        </div>
      {/if}
    </div>
  </div>
{/if}

<style>
  /* Kept rules: every rule draws the gallery the plugin ships (grid, tiles, play icon, overlay, lightbox and its zoom-in
     animation); no selector reaches Bootstrap or theme markup. */
  .media-page-gallery-page__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.25rem;
  }

  .media-page-gallery-page__item {
    cursor: pointer;
    border-radius: 12px;
    overflow: hidden;
    background: #000;
  }

  .media-page-gallery-page__item-inner {
    position: relative;
    aspect-ratio: 16/10;
    transition: transform 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  }

  .media-page-gallery-page__item:hover .media-page-gallery-page__item-inner {
    transform: scale(1.05);
  }

  .media-page-gallery-page__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: filter 0.3s ease;
  }

  .media-page-gallery-page__item:hover .media-page-gallery-page__image,
  .media-page-gallery-page__item:hover .media-page-gallery-page__video-thumb video {
    filter: brightness(0.6);
  }

  .media-page-gallery-page__video-thumb {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: #1a1a1a;
    color: #fff;
    position: relative;
    overflow: hidden;
  }

  .media-page-gallery-page__video-thumb video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: filter 0.3s ease;
  }

  .media-page-gallery-page__play-icon {
    width: 60px;
    height: 60px;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(5px);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    padding-left: 5px; /* Visual centering for play icon */
    transition: transform 0.3s ease, background 0.3s ease;
  }

  .media-page-gallery-page__item:hover .media-page-gallery-page__play-icon {
    transform: scale(1.1);
    background: var(--pano-color-primary, #0d6efd);
  }

  .media-page-gallery-page__badge {
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .media-page-gallery-page__overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    background: rgba(0, 0, 0, 0.3);
    opacity: 0;
    transition: opacity 0.3s ease;
    padding: 1.5rem;
  }

  .media-page-gallery-page__item:hover .media-page-gallery-page__overlay {
    opacity: 1;
  }

  .media-page-gallery-page__item-title {
    color: #fff;
    font-weight: 600;
    font-size: 1.1rem;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  }

  .media-page-gallery-page__zoom-icon {
    color: rgba(255, 255, 255, 0.8);
    font-size: 1.25rem;
  }

  /* Lightbox */
  .media-page-gallery-page__lightbox {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.95);
    backdrop-filter: blur(10px);
    z-index: 10000;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: zoom-out;
  }

  .media-page-gallery-page__lightbox-nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #fff;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    cursor: pointer;
    transition: all 0.2s ease;
    z-index: 10;
  }

  .media-page-gallery-page__lightbox-nav:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-50%) scale(1.1);
  }

  .media-page-gallery-page__nav-prev { left: 2rem; }
  .media-page-gallery-page__nav-next { right: 2rem; }

  .media-page-gallery-page__lightbox-close {
    position: absolute;
    top: 2rem;
    right: 2rem;
    background: none;
    border: none;
    color: #fff;
    font-size: 2rem;
    cursor: pointer;
    opacity: 0.5;
    transition: opacity 0.2s;
    z-index: 10;
  }

  .media-page-gallery-page__lightbox-close:hover {
    opacity: 1;
  }

  .media-page-gallery-page__lightbox-main {
    max-width: 90vw;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    cursor: default;
    animation: media-page-zoom-in 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
  }

  @keyframes media-page-zoom-in {
    from { opacity: 0; transform: scale(0.95); }
    to { opacity: 1; transform: scale(1); }
  }

  .media-page-gallery-page__media-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;
    min-height: 200px;
  }

  .media-page-gallery-page__media-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
  }

  .media-page-gallery-page__lightbox-media {
    max-width: 100%;
    max-height: 80vh;
    object-fit: contain;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
    border-radius: 4px;
  }

  .media-page-gallery-page__lightbox-details {
    margin-top: 1.5rem;
    color: #fff;
    text-align: center;
  }

  .media-page-gallery-page__details-inner {
    display: inline-block;
    padding: 1rem 2rem;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    border-radius: 100px;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  @media (max-width: 768px) {
    .media-page-gallery-page__lightbox-nav {
      width: 44px;
      height: 44px;
      font-size: 1rem;
    }
    .media-page-gallery-page__nav-prev { left: 1rem; }
    .media-page-gallery-page__nav-next { right: 1rem; }
    .media-page-gallery-page__details-inner {
      border-radius: 12px;
      padding: 1rem;
    }
  }
</style>

<script context="module">
    import { api } from '@panomc/sdk/plugin-api';
    import {error} from '@panomc/sdk/svelte';

    export async function load(event) {
    const { parent, url } = event;
    await parent();

    const pathParts = url.pathname.split('/');
    const slug = pathParts[pathParts.length - 1];

    const res = await api.get({
      path: `/media-pages/${slug}`,
      request: event,
    });

    if (res.error || !res.mediaPage) {
      return error(404, 'Page not found');
    }

    const data = res.data || res;

    return {
      data: {
        mediaPage: data.mediaPage,
        categories: data.categories || [],
      },
      pageTitle: { title: data.mediaPage.title, subtitle: data.mediaPage.description },
    };
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { NoContent } from '@panomc/sdk/components/theme';
  import { fade } from 'svelte/transition';
  import { derived } from 'svelte/store';
  import { _ as i18n } from 'svelte-i18n';

  // Views may not import ../../main (plugin-kit rule V1), so the plugin prefix lives here.
  const pluginId = 'pano-plugin-media-page';
  const _ = derived(i18n, ($t) => (key, options) => $t(`plugins.${pluginId}.${key}`, options));

  export let data;

  let lightboxItem = null;

  // Flatten items for easy navigation
  $: allItems = data.categories.reduce((acc, cat) => [...acc, ...cat.items], []);

  function openLightbox(item) {
    lightboxItem = item;
    document.body.style.overflow = 'hidden';
  }

  function closeLightbox() {
    lightboxItem = null;
    document.body.style.overflow = '';
  }

  function nextMedia() {
    if (!lightboxItem) return;
    const currentIndex = allItems.findIndex(i => i.id === lightboxItem.id);
    const nextIndex = (currentIndex + 1) % allItems.length;
    lightboxItem = allItems[nextIndex];
  }

  function prevMedia() {
    if (!lightboxItem) return;
    const currentIndex = allItems.findIndex(i => i.id === lightboxItem.id);
    const prevIndex = (currentIndex - 1 + allItems.length) % allItems.length;
    lightboxItem = allItems[prevIndex];
  }

  // Not <svelte:window>: it compiles to the runtime's `window` export, which the theme's shared
  // runtime shim reads before Svelte sets it, so the whole page failed to mount.
  onMount(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  });

  function handleKeyDown(event) {
    if (!lightboxItem) return;
    
    if (event.key === 'ArrowRight') {
      nextMedia();
    } else if (event.key === 'ArrowLeft') {
      prevMedia();
    } else if (event.key === 'Escape') {
      closeLightbox();
    }
  }
</script>
