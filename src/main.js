import { PanoPlugin, viewComponent } from '@panomc/sdk';
import { derived } from 'svelte/store';
import { _ as i18n } from '@panomc/sdk/utils/language';
import ApiUtil from '@panomc/sdk/utils/api';
import { showToast } from '@panomc/sdk/toasts';

export const pluginId = 'pano-plugin-media-page';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

// Success/failure colouring for this plugin's toasts, matching the panel. showToast from
// @panomc/sdk/toasts is the host panel's ToastContainer `show`, whose signature is
// (text, params, toastComponent, options): passing undefined for toastComponent keeps the
// host's DefaultToast, and options.variant maps to Bootstrap's text-success / text-danger.
// These live here rather than in @panomc/sdk/toasts because this plugin is pinned to
// @panomc/sdk 1.0.0-dev.39, which predates the variants; they can be dropped for a direct
// SDK import once that pin moves. On an older panel build the extra argument is ignored and
// the toast renders neutral, so this degrades instead of breaking.
export function showSuccessToast(text, params = {}) {
  return showToast(text, params, undefined, { variant: 'success' });
}

export function showErrorToast(text, params = {}) {
  return showToast(text, params, undefined, { variant: 'danger' });
}

export default class MediaPagePlugin extends PanoPlugin {
  async onLoad() {
    const pano = this.pano;

    if (pano.isPanel) {
      // Register Panel Pages
      pano.ui.page.register({
        path: '/media-pages',
        component: viewComponent(() => import('./panel/pages/MediaPagesListPage.svelte')),
        permission: `pano.plugin.${pluginId}.manage.media.page`,
      });

      pano.ui.page.register({
        path: '/media-pages/create',
        component: viewComponent(() => import('./panel/pages/MediaPageEditorPage.svelte')),
        permission: `pano.plugin.${pluginId}.manage.media.page`,
      });

      pano.ui.page.register({
        path: '/media-pages/edit/:id',
        component: viewComponent(() => import('./panel/pages/MediaPageEditorPage.svelte')),
        permission: `pano.plugin.${pluginId}.manage.media.page`,
      });

      // Register Panel Sidebar Navigation
      pano.ui.nav.site.editNavLinks((navigationItems) => {
        // Place after "Görünüm" (view/appearance)
        const viewIndex = navigationItems.findIndex(
          (item) => item.href === '/view'
        );

        const mediaPageLink = {
          href: '/media-pages',
          icon: 'fas fa-photo-film',
          text: `plugins.${pluginId}.nav`,
          startsWith: true,
          permission: `pano.plugin.${pluginId}.manage.media.page`,
        };

        if (viewIndex !== -1) {
          navigationItems.splice(viewIndex + 1, 0, mediaPageLink);
        } else {
          navigationItems.push(mediaPageLink);
        }

        return navigationItems;
      });
    } else {
      // Theme Side
      const galleryPageComponent = viewComponent(() => import('./theme/pages/GalleryPage.svelte'));

      pano.ui.app.onLoad(async (data, event) => {
        try {
          const res = await ApiUtil.get({ path: '/api/media-pages', request: event });
          if (res && res.mediaPages) {
            const pages = res.mediaPages;

            pages.forEach((mediaPage) => {
              // Register dynamic route for each media page
              pano.ui.page.register({
                path: `/gallery/${mediaPage.slug}`,
                component: galleryPageComponent,
              });

              // If registered to theme navigation
              if (mediaPage.registerToThemeNav) {
                if (pano.ui.nav.site.editNavLinks) {
                  pano.ui.nav.site.editNavLinks((navItems) => {
                    if (!navItems.find((n) => n.href === `/gallery/${mediaPage.slug}`)) {
                      navItems.push({
                        href: `/gallery/${mediaPage.slug}`,
                        text: mediaPage.title,
                        startsWith: false,
                      });
                    }
                    return navItems;
                  });
                }
              }
            });
          }
        } catch (e) {
          console.error('[MediaPagePlugin] Failed to fetch media pages for route registration', e);
        }
      });
    }
  }

  onContextUpdate(ctx) { }

  onUnload() { }
}
