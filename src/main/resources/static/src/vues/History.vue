<template>
    <div>
        <button @click="showHistory" type="button" class="btn btn-default">履歴を確認する</button>
        <div class="modal fade" id="js-history-modal" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                        <h4 class="modal-title">
                            <strong>作成したPDFの履歴管理</strong><br>
                            <small>(データはブラウザに保存しております。最大20件の作成履歴を表示します。)</small>
                        </h4>
                    </div>
                    <div class="modal-body">
                        <ul v-if="0 < historyDatas.length">
                            <li class="history-list" v-for="(history, index) in historyDatas" :key="history.id">
                                <div>{{ history.formatedCreatedAt }}に作成した{{ templateTypeMapper[history.templateType] }}</div>
                                <a :href="history.blobUrl" target="_blank">ファイルを開く</a>/
                                <a @click="restoreTable(history.tableData,history.templateType)">テーブルを復元</a>/
                                <a @click="deleteHistory(history.id)">履歴を削除</a>
                            </li>
                        </ul>
                        <h5 v-else>履歴がありません。</h5>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
global.jQuery = require('jquery');
const $ = global.jQuery;
require('bootstrap');
import moment from 'moment';
export default {
    name: 'history',
    data() {
        return {
            show: false,
            historyDatas: [],
            templateTypeMapper: {
                letterpack: 'レターパック'
            }
        }
    },
    props: {
        labelmakerDb: null,
        historyTable: null
    },
    methods: {
        showHistory: function () {
            $('#js-history-modal').modal('show');
            this.getHistorys();
        },
        getHistorys: function () {
            let self = this;
            if (!_.isNull(self.labelmakerDb) && !_.isNull(self.historyTable)) {
                self.labelmakerDb.select().from(self.historyTable).limit(20).orderBy(self.historyTable.id, lf.Order.DESC).exec()
                    .then(function (results) {
                        if (!_.isEmpty(results)) {
                            results.forEach(function (result) {
                                result.blobUrl = window.URL.createObjectURL(new Blob([result.pdfData], {
                                    type: 'application/pdf'
                                }));
                                result.formatedCreatedAt = moment(result.createdAt).format('YYYY年MM月DD日HH時');
                            });
                            self.historyDatas = results;
                        } else {
                            self.historyDatas = [];
                        }
                    });
            }
        },
        restoreTable: function (tableData, templateType) {
            this.$emit('restoreTableData', tableData, templateType);
            $('#js-history-modal').modal('hide');
        },
        deleteHistory: function (id) {
            let self = this;
            if (!_.isNull(self.labelmakerDb) && !_.isNull(self.historyTable)) {
                self.labelmakerDb.delete().from(self.historyTable).where(self.historyTable.id.eq('id')).exec()
                    .then(function () {
                        alert('履歴を削除しました');
                        self.getHistorys();
                    });
            }
        }
    },
    created: function () { },
    mounted: function () { },
    beforeUpdate: function () { },
    updated: function () { }
}
</script>

<style lang="scss" scoped>
.history-list {
    margin-top: 10px;
    padding: 10px;
    border-bottom: 1px solid #ddd;
    div {
        margin-bottom: 10px;
    }
    a {
        margin: 0 10px 0 0;
    }
}
</style>
