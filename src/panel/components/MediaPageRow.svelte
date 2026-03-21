<tr>
  <th scope="row" class="align-middle text-center" style="width: 60px;">
    <div class="dropdown position-static">
      <button
        type="button"
        class="btn btn-link"
        aria-expanded="false"
        aria-haspopup="true"
        data-bs-toggle="dropdown"
        use:tooltip={[$_('media-pages.list.table.actions')]}
        aria-label={$_('media-pages.list.table.actions')}>
        <span class="fas fa-ellipsis-v"></span>
      </button>
      <div class="dropdown-menu dropdown-menu-start">
        <button type="button" class="dropdown-item" on:click={() => onEditClick(mediaPage.id)}>
          <span>
            <i class="fas fa-edit me-2"></i>
            {$_('media-pages.list.table.actions-edit')}
          </span>
        </button>
        <button type="button" class="dropdown-item link-danger" on:click={() => onDeleteClick(mediaPage.id)}>
          <i class="fas fa-trash me-2"></i>
          <span> {$_('media-pages.list.table.actions-delete')} </span>
        </button>
      </div>
    </div>
  </th>
  <td class="align-middle" style="width: 80px;">
    <code>#{mediaPage.id}</code>
  </td>
  <td class="align-middle">
    <button
      type="button"
      on:click={() => onEditClick(mediaPage.id)}
      use:tooltip={[$_('media-pages.list.table.actions-edit')]}
      title={mediaPage.title}
      aria-label={mediaPage.title}
      class="btn btn-link p-0 text-start text-decoration-none w-100 text-truncate focus-ring">
      {mediaPage.title}
    </button>
  </td>
  <td class="align-middle text-muted small">{mediaPage.slug}</td>
  <td class="align-middle">
    {#if mediaPage.active}
      <span class="badge text-bg-success">
        {$_('media-pages.list.status.active')}
      </span>
    {:else}
      <span class="badge text-bg-danger">
        {$_('media-pages.list.status.passive')}
      </span>
    {/if}
  </td>
</tr>

<script>
  import { _ } from '../../main';
  import tooltip from '@panomc/sdk/utils/tooltip';

  export let mediaPage;
  export let onEditClick;
  export let onDeleteClick;
</script>
