import { PanoPlugin, viewComponent } from '@panomc/sdk';
import { derived } from 'svelte/store';
import { _ as i18n } from '@panomc/sdk/utils/language';
import ApiUtil from '@panomc/sdk/utils/api';

export const pluginId = 'pano-plugin-media-page';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

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
