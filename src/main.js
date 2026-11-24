import { PanoPlugin, viewComponent } from '@panomc/sdk';
import { derived, get } from 'svelte/store';
import { _ as i18n, currentLanguage } from '@panomc/sdk/utils/language';
import { api } from '@panomc/sdk/plugin-api';
import { showToast } from '@panomc/sdk/toasts';

export const pluginId = 'pano-plugin-media-page';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

// Turns a server error ({ code }) into this plugin's translated errors.<code> text. Unknown
// codes fall back to errors.ERROR_UNKNOWN, never to the server's English message.
export function translateError(error) {
  const t = get(_);
  const code = error?.code;
  if (!code) return t('errors.ERROR_UNKNOWN');
  const key = `errors.${code}`;
  const text = t(key);
  return text && text !== `plugins.${pluginId}.${key}` ? text : t('errors.ERROR_UNKNOWN');
}

// Text for unexpected failures (network errors, exceptions).
export function generalError() {
  return get(_)('errors.ERROR_GENERAL');
}

const FILE_SIZE_UNITS = ['byte', 'kilobyte', 'megabyte', 'gigabyte'];

// Formats a byte count with the panel language's decimal separator and unit names
// (e.g. "1.5 MB" in English, "1,5 MB" in Turkish, "1,5 МБ" in Russian).
export function formatFileSize(bytes) {
  const lang = (currentLanguage && get(currentLanguage)?.code) || 'en-US';
  const i = bytes ? Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), FILE_SIZE_UNITS.length - 1) : 0;
  const value = bytes ? bytes / Math.pow(1024, i) : 0;
  try {
    return new Intl.NumberFormat(lang, {
      style: 'unit',
      unit: FILE_SIZE_UNITS[i],
      unitDisplay: 'short',
      maximumFractionDigits: 1,
    }).format(value);
  } catch (e) {
    return parseFloat(value.toFixed(1)) + ' ' + ['B', 'KB', 'MB', 'GB'][i];
  }
}

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
      pano.ui.app.onLoad(async (data, event) => {
        try {
          const res = await api.get({ path: '/media-pages', request: event });
          if (res && res.items) {
            const pages = res.items;

            pages.forEach((mediaPage) => {
              // Register dynamic route for each media page
              pano.ui.page.register({
                path: `/gallery/${mediaPage.slug}`,
                view: 'media-page:GalleryPage',
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
