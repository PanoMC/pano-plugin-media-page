// Sample data of GalleryPage for the view catalogue (doc 02 section 7). Pure data: import only view helpers and
// relative .js fixtures.
/** @type {string[]} */
export const notApplicable = ['error', 'loading'];

/** @type {import('@panomc/plugin-kit').Samples} */
export default {
  filled: {
    props: {
      data: {
        categories: [
          {
            category: { id: 1, title: 'Screenshots', description: 'Builds from the survival world.' },
            items: [
              { id: 1, type: 'IMAGE', title: 'Spawn', description: 'The new spawn', filePath: 'spawn.png', thumbnailPath: null },
              { id: 2, type: 'VIDEO', title: 'Trailer', description: '', filePath: 'trailer.mp4', thumbnailPath: null },
            ],
          },
          {
            category: { id: 2, title: 'Events', description: '' },
            items: [],
          },
        ],
      },
    },
  },
  empty: {
    props: { data: { categories: [] } },
  },
};
