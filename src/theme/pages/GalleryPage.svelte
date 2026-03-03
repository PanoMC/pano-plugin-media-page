<svelte:component this={PageTitle} title={data.mediaPage.title} />

<article class="container pb-5">
  <header class="gallery-header mb-5">
    {#if data.mediaPage.description}
      <p class="lead text-muted">{data.mediaPage.description}</p>
    {/if}
  </header>

  {#if data.categories && data.categories.length > 0}
    {#each data.categories as cat (cat.category.id)}
      <section class="mb-5 category-section">
        <div class="category-header mb-4">
          <h2 class="h3 mb-1 fw-bold">{cat.category.title}</h2>
          {#if cat.category.description}
            <p class="text-muted small mb-0">{cat.category.description}</p>
          {/if}
          <div class="header-line mt-2"></div>
        </div>

        {#if cat.items && cat.items.length > 0}
          <div class="gallery-grid">
            {#each cat.items as item (item.id)}
              <div class="gallery-item" on:click={() => openLightbox(item)} on:keydown={(e) => { if (e.key === 'Enter') openLightbox(item); }} role="button" tabindex="0">
                <div class="item-inner">
                  {#if item.type === 'IMAGE'}
                    <img
                      src="/api/media/file/{item.thumbnailPath || item.filePath}"
                      alt={item.title}
                      loading="lazy"
                      class="gallery-img" />
                  {:else}
                    <div class="gallery-video-thumb">
                      {#if item.thumbnailPath}
                        <img src="/api/media/file/{item.thumbnailPath}" alt={item.title} loading="lazy" class="gallery-img" />
                      {:else}
                        <video src="/api/media/file/{item.filePath}#t=0.1" preload="metadata" muted></video>
                      {/if}
                      <div class="play-icon">
                        <i class="fas fa-play"></i>
                      </div>
                      <span class="badge bg-blur position-absolute bottom-0 end-0 m-2">
                        <i class="fas fa-video me-1"></i>Video
                      </span>
                    </div>
                  {/if}
                  <div class="gallery-overlay">
                    <div class="overlay-content">
                      {#if item.title}
                        <span class="gallery-title d-block">{item.title}</span>
                      {/if}
                      <div class="zoom-icon mt-2">
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
    <div class="py-5">
      <NoContent />
    </div>
  {/if}
</article>

<!-- Lightbox -->
{#if lightboxItem}
  <div class="lightbox-backdrop" on:click={closeLightbox} transition:fade={{ duration: 200 }}>
    <button type="button" class="lightbox-nav nav-prev" on:click|stopPropagation={prevMedia} aria-label="Previous">
      <i class="fas fa-chevron-left"></i>
    </button>
    
    <button type="button" class="lightbox-nav nav-next" on:click|stopPropagation={nextMedia} aria-label="Next">
      <i class="fas fa-chevron-right"></i>
    </button>

    <button type="button" class="lightbox-close-btn" on:click={closeLightbox} aria-label="Close">
      <i class="fas fa-times"></i>
    </button>

    <div class="lightbox-main" on:click|stopPropagation role="dialog">
      <div class="media-container">
        {#key lightboxItem.id}
          <div in:fade={{ duration: 250, delay: 50 }} out:fade={{ duration: 150 }} class="media-wrapper">
            {#if lightboxItem.type === 'IMAGE'}
              <img src="/api/media/file/{lightboxItem.filePath}" alt={lightboxItem.title} class="lightbox-media" />
            {:else}
              <video src="/api/media/file/{lightboxItem.filePath}" controls autoplay class="lightbox-media">
                <track kind="captions" />
              </video>
            {/if}
          </div>
        {/key}
      </div>

      {#if lightboxItem.title || lightboxItem.description}
        <div class="lightbox-details">
          <div class="details-inner">
            {#if lightboxItem.title}
              <h4 class="mb-1 fw-bold">{lightboxItem.title}</h4>
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

<svelte:window on:keydown={handleKeyDown} />

<style>
  .header-line {
    width: 40px;
    height: 3px;
    background: var(--bs-primary, #0d6efd);
    border-radius: 2px;
  }

  .gallery-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.25rem;
  }

  .gallery-item {
    cursor: pointer;
    border-radius: 12px;
    overflow: hidden;
    background: #000;
  }

  .item-inner {
    position: relative;
    aspect-ratio: 16/10;
    transition: transform 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  }

  .gallery-item:hover .item-inner {
    transform: scale(1.05);
  }

  .gallery-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: filter 0.3s ease;
  }

  .gallery-item:hover .gallery-img,
  .gallery-item:hover .gallery-video-thumb video {
    filter: brightness(0.6);
  }

  .gallery-video-thumb {
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

  .gallery-video-thumb video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: filter 0.3s ease;
  }

  .play-icon {
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

  .gallery-item:hover .play-icon {
    transform: scale(1.1);
    background: var(--bs-primary, #0d6efd);
  }

  .bg-blur {
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .gallery-overlay {
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

  .gallery-item:hover .gallery-overlay {
    opacity: 1;
  }

  .gallery-title {
    color: #fff;
    font-weight: 600;
    font-size: 1.1rem;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  }

  .zoom-icon {
    color: rgba(255, 255, 255, 0.8);
    font-size: 1.25rem;
  }

  /* Lightbox */
  .lightbox-backdrop {
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

  .lightbox-nav {
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

  .lightbox-nav:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-50%) scale(1.1);
  }

  .nav-prev { left: 2rem; }
  .nav-next { right: 2rem; }

  .lightbox-close-btn {
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

  .lightbox-close-btn:hover {
    opacity: 1;
  }

  .lightbox-main {
    max-width: 90vw;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    cursor: default;
    animation: zoomIn 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
  }

  @keyframes zoomIn {
    from { opacity: 0; transform: scale(0.95); }
    to { opacity: 1; transform: scale(1); }
  }

  .media-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;
    min-height: 200px;
  }

  .media-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
  }

  .lightbox-media {
    max-width: 100%;
    max-height: 80vh;
    object-fit: contain;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
    border-radius: 4px;
  }

  .lightbox-details {
    margin-top: 1.5rem;
    color: #fff;
    text-align: center;
  }

  .details-inner {
    display: inline-block;
    padding: 1rem 2rem;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    border-radius: 100px;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  @media (max-width: 768px) {
    .lightbox-nav {
      width: 44px;
      height: 44px;
      font-size: 1rem;
    }
    .nav-prev { left: 1rem; }
    .nav-next { right: 1rem; }
    .details-inner {
      border-radius: 12px;
      padding: 1rem;
    }
  }
</style>

<script context="module">
  import ApiUtil from '@panomc/sdk/utils/api';
  import { error } from '@panomc/sdk/svelte';

  export async function load(event) {
    const { parent, url } = event;
    const { pageTitle } = await parent();

    const pathParts = url.pathname.split('/');
    const slug = pathParts[pathParts.length - 1];

    const res = await ApiUtil.get({
      path: `/api/media-pages/${slug}`,
      request: event,
    });

    if (res.error || !res.mediaPage) {
      return error(404, 'Page not found');
    }

    const data = res.data || res;
    pageTitle.set(data.mediaPage.title);

    return {
      data: {
        mediaPage: data.mediaPage,
        categories: data.categories || [],
      },
    };
  }
</script>

<script>
  import { PageTitle, NoContent } from '@panomc/sdk/components/theme';
  import { fade } from 'svelte/transition';

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
