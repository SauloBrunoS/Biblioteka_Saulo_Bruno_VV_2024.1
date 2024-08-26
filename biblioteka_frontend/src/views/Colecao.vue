<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import type { Pagination } from '@/api/adapters/BaseAdapters';
import type { Colecao, InfoDataTableServer } from '@/types';
import type { DataTableHeader } from '@/types/vuetify';
import { reactive } from 'vue';
import { useNotificationStore } from '@/stores/Notification';
import ColecaoService from '@/api/ColecaoService';

const constant: {
    cabecalhoColecoes: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoColecoes: [
        { title: "Nome", key: "nome", sortable: true },
        { title: "Descricao", key: "descricao", sortable: true },
        { title: "", key: "id", sortable: false }
    ],
    itemsPerPageOptions: [
        { value: 5, title: '5' },
        { value: 10, title: '10' },
        { value: 25, title: '25' },
        { value: 50, title: '50' },
        { value: 100, title: '100' }
    ],
    notificationStore: useNotificationStore()
}

const state = reactive({
    pagination: { pageSize: 5 } as Pagination,
    search: "" as string,
    dialogForm: false as boolean,
    dialogDelete: false,
    colecao: {} as Colecao,
    listaColecoes: [] as Colecao[],
    infoDataTableServer: {} as InfoDataTableServer,
    idColecao: null as unknown as number
})

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    ColecaoService.findSearch(page, itemsPerPage, sortBy, search)
        .then(({ items: colecoes, pagination: data }) => {
            state.listaColecoes = colecoes
            state.pagination.page = data.number
            state.pagination.total = data.totalElements
            state.pagination.pageCount = data.totalPages
        });
}


function atualizar() {
    loadItems(state.infoDataTableServer);
}

function atualizarDialogForm() {
    state.dialogForm = false;
    atualizar();
}

function atualizarDialogDelete() {
    state.dialogDelete = !state.dialogDelete;
}

function abrirDialogDelete(id: number) {
    state.idColecao = id;
    atualizarDialogDelete();
}

async function deletarItem(id: number) {
    try {
        await ColecaoService.delete(id);
        loadItems(state.infoDataTableServer);
        atualizarDialogDelete();
        constant.notificationStore.notificar({ mensagem: "Coleção excluída com sucesso", tipoMensagem: "success", visibilidade: true })
    } catch (err) {
        constant.notificationStore.notificar({ mensagem: "Erro ao excluir a Coleção!", tipoMensagem: "error", visibilidade: true })
    }
}


function abrirDialogForm(colecao: Colecao | null) {
    if (colecao != null) {
        state.idColecao = colecao.id;
        state.colecao = colecao
    }
    state.colecao = colecao as unknown as Colecao
    state.dialogForm = true;
}

</script>

<template>
    <v-card-text>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoColecoes" :items="state.listaColecoes"
            :items-per-page="state.pagination.pageSize" :items-length="state.pagination.total"
            :items-per-page-options="constant.itemsPerPageOptions" @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center ">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model="state.search"
                        :flat="true" label="Filtrar" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center"
                            style="font-family: 'Lucida Sans', 'Lucida Sans Regular', 'Lucida Grande', 'Lucida Sans Unicode', Geneva, Verdana, sans-serif; font-weight: bold;"
                            color="primary" elevation="0" @click="abrirDialogForm(null)">
                            Adicionar coleção
                        </v-btn>
                    </div>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr>
                    <td>{{ item.nome }}</td>
                    <td>{{ item.descricao }}</td>
                </tr>
            </template>
        </v-data-table-server>
    </v-card-text>
</template>
