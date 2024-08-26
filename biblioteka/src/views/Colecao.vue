<script setup lang="ts">
import type { Pagination } from '@/api/adapters/BaseAdapters';
import type { Colecao, InfoDataTableServer } from '@/types';
import type { DataTableHeader } from '@/types/vuetify';
import { reactive } from 'vue';

const constant: {
    cabecalhoColecoes: DataTableHeader[];
    itemsPerPageOptions: any[];
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
    ]
}

const state = reactive({
    pagination: { pageSize: 5 } as Pagination,
    search: "" as string,
    dialogForm: false as boolean,
    dialogDelete: false,
    colecao: {} as Colecao,
    infoDataTableServer: {} as InfoDataTableServer
})

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    colecaoService.findSearch(page, itemsPerPage, sortBy, search, route.params.id, state.status)
        .then(({ items: atendimentos, pagination: data }) => {
            state.listaAtendimentos = atendimentos
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


async function abrirDialogForm(atendimento) {
    if (!atendimento) {
        const atendimentosEmAndamento = state.listaAtendimentos.filter(at => at.status === 'EM_ANDAMENTO');
        if (atendimentosEmAndamento.length > 0) {
            constant.notificationStore.notificar({
                mensagem: "Já existe um atendimento em andamento!",
                tipoMensagem: "error",
                visibilidade: true,
            });
            return;
        }
    }
    if (atendimento != null) {
        state.idAtendimento = atendimento.id;
    }
    state.atendimento = atendimento
    state.dialogForm = true;
}

const voltarParaAtendimentos = () => {
    state.exibirDetalhesAtendimento = false;
};

watchEffect(() => {
    loadItems(state.infoDataTableServer);
});

function atualizarDialogDelete() {
    state.dialogDelete = !state.dialogDelete;
}

function abrirDialogDelete(id: number) {
    state.idAtendimento = id;
    atualizarDialogDelete();
}

async function deletarItem(id: number) {
    try {
        await atendimentoService.delete(id);
        loadItems(state.infoDataTableServer);
        atualizarDialogDelete();
        constant.notificationStore.notificar({ mensagem: "Atendimento excluído com sucesso", tipoMensagem: "success", visibilidade: true })
    } catch (err) {
        constant.notificationStore.notificar({ mensagem: "Erro ao excluir o Atendimento!", tipoMensagem: "error", visibilidade: true })
    }
}

</script>

<template>
    <atendimento-detalhes v-if="state.exibirDetalhesAtendimento" :atendimento-id="state.idAtendimento"
        @voltar-para-atendimentos="voltarParaAtendimentos()" />
    <v-card-text v-else>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoAtendimentos"
            :items="state.listaAtendimentos" :items-per-page="state.pagination.pageSize"
            :items-length="state.pagination.total" :items-per-page-options="constant.itemsPerPageOptions"
            @update:options="loadItems">
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
                    <Field name="status" v-model="state.status" v-slot="{ field, errors }">
                        <v-select hide-details="auto" @keydown.prevent label="Filtrar por Status" variant="outlined"
                            v-bind="field" :items="constant.statusAtendimento" item-title="value" item-value="key"
                            v-model="state.selectedStatus" :error-messages="errors" data-cy="status" />

                    </Field>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="primary" elevation="0"
                            style="height: 60px" @click="abrirDialogForm(null)" data-cy="Adicionar atendimento">
                            Adicionar atendimento
                        </v-btn>
                    </div>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr>
                    <td>{{ dayjs(item.data).format('DD/MM/YYYY') }}</td>
                    <td>{{ item.clinica.nome }}</td>
                    <td>{{ item.responsavel.nome }}</td>
                    <td>{{ StatusAtendimento[item.status as keyof StatusAtendimento] }}</td>
                    <td>
                        <div>
                            <v-tooltip text="Detalhes do Atendimento" location="top">
                                <template v-slot:activator="{ props }">
                                    <v-btn-details @click="exibirDetalhesAtendimento(item.id)" data-cy="btnDetalhes"
                                        v-bind="props"></v-btn-details>
                                </template>
                            </v-tooltip>

                            <v-tooltip text="Editar" location="top">
                                <template v-slot:activator="{ props }">
                                    <v-btn-edit v-if="item.status == 'EM_ANDAMENTO'" @click="abrirDialogForm(item)"
                                        data-cy="btnEditar" v-bind="props"></v-btn-edit>
                                </template>
                            </v-tooltip>


                            <v-tooltip text="Excluir" location="top">
                                <template v-slot:activator="{ props }">
                                    <v-btn-delete v-if="item.status == 'EM_ANDAMENTO'"
                                        @click="abrirDialogDelete(item.id)" data-cy="btnDeletar"
                                        v-bind="props"></v-btn-delete>
                                </template>
                            </v-tooltip>
                        </div>
                    </td>
                </tr>
            </template>
        </v-data-table-server>
    </v-card-text>

    <atendimento-form :dialog-Visible="state.dialogForm" :atendimento="state.atendimento"
        @submitted="atualizarDialogForm()" @canceled="state.dialogForm = !state.dialogForm"
        :atendimento-id="state.idAtendimento"></atendimento-form>

    <dialog-delete v-model:dialog-visible="state.dialogDelete" @canceled="atualizarDialogDelete()"
        @submitted="deletarItem(state.idAtendimento)"
        :descricao="`Você tem certeza que deseja excluir este atendimento?`"></dialog-delete>
</template>
