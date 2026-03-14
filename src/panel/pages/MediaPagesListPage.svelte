<article class="container vstack gap-3">
  <!-- Action Menu -->
  <PageActions leftClasses="d-lg-flex d-none" middleClasses="d-lg-flex d-none">
    <div slot="right">
      <button type="button" class="btn btn-secondary" on:click={onCreateClick}>
        <i class="fas fa-plus"></i>
        <span class="d-lg-inline d-none ms-2"> {$_('media-pages.list.add-page')}</span>
      </button>
    </div>
  </PageActions>

  <div class="card">
    <div class="card-header">
      {$_('media-pages.list.total-count', { values: { count: data.pageCount || 0 } })}
    </div>

    {#if !data.mediaPages || data.mediaPages.length === 0}
      <NoContent />
    {:else}
      <div class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col" style="width: 60px;"></th>
              <th scope="col" style="width: 80px;">{$_('media-pages.list.table.id')}</th>
              <th scope="col">{$_('media-pages.list.table.title')}</th>
              <th scope="col">{$_('media-pages.list.table.slug')}</th>
              <th scope="col">{$_('media-pages.list.table.status')}</th>
            </tr>
          </thead>
          <tbody>
            {#each data.mediaPages as mediaPage (mediaPage.id)}
              <MediaPageRow {mediaPage} {onEditClick} {onDeleteClick} />
            {/each}
          </tbody>
        </table>
      </div>
      <div class="card-footer">
        <Pagination
          page={data.page}
          totalPage={data.totalPage}
          on:firstPageClick={() => onPageClick(1)}
          on:lastPageClick={() => onPageClick(data.totalPage)}
          on:pageLinkClick={(event) => onPageClick(event.detail.page)} />
      </div>
    {/if}
  </div>

  <ConfirmDeletePageModal />
</article>

<script context="module">
  import ApiUtil from '@panomc/sdk/utils/api';
  import { pluginId } from '../../main';

  export async function load(event) {
    const {
      parent,
      url: { searchParams },
    } = event;
    const { pageTitle } = await parent();

    pageTitle.set(`plugins.${pluginId}.media-pages.list.title`);

    const page = searchParams.get('page') || 1;

    const body = await ApiUtil.get({
      path: `/api/panel/media-pages?page=${page}`,
      request: event,
    });

    if (body.error) {
      return { data: { mediaPages: [], totalPage: 1, page: 1, pageCount: 0 } };
    }

    const data = body.data || body;
    data.page = parseInt(page);

    return { data: data };
  }
</script>

<script>
  import { base, goto } from '@panomc/sdk/svelte';
  import { PageActions, NoContent, Pagination } from '@panomc/sdk/components/panel';

  import { _ } from '../../main';
  import MediaPageRow from '../components/MediaPageRow.svelte';
  import ConfirmDeletePageModal, {
    show as showDeleteModal,
    setCallback as setDeleteCallback,
  } from '../components/modals/ConfirmDeletePageModal.svelte';

  export let data;

  async function refreshData() {
    const pageNum = data.page === 1 ? '' : `?page=${data.page}`;
    await goto(`${base}/media-pages${pageNum}`, { invalidateAll: true });
  }

  async function onPageClick(pageNum) {
    data.page = pageNum;
    await refreshData();
  }

  function onCreateClick() {
    goto(`${base}/media-pages/create`);
  }

  function onEditClick(id) {
    goto(`${base}/media-pages/edit/${id}`);
  }

  function onDeleteClick(id) {
    const mediaPage = data.mediaPages.find((p) => p.id === id);
    if (mediaPage) {
      showDeleteModal(mediaPage);
    }
  }

  setDeleteCallback(() => {
    if (data.mediaPages.length === 1 && data.page > 1) {
      data.page--;
    }
    refreshData();
  });
</script>
