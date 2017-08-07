<template>
  <div id="app" class="container" data-intro="レターパックなどの記入を効率的に行うためのサービスです。<br>エクセルの操作感で一度に最大50件のラベルを作成できます。" data-step="1">
    <div id="menu" class="row">
      <div class="form-group">
        <div class="col-xs-12 control-label">
          <div class="form-group">
            <div class="col-xs-2">
              <label>初めての方</label>
              <button @click="startTutorial" type="button" class="btn btn-default">使い方を見る</button>
            </div>
            <div class="col-xs-2">
              <label>テンプレートを選択</label>
              <select data-intro="作成したいテンプレートを選択してください。<br><br>(現在レターパックのみ)" data-step="2" name="template" v-model="selected" 　@change="selectTemplate" class="form-control">
                <option value="letterpack">レターパック</option>
              </select>
            </div>
            <div class="col-xs-2">
              <label>シートからPDFを作成</label>
              <button data-intro="このシートへの記入が終了したらこのボタンを押してPDFをダウンロードしてください。" data-step="4" type="button" @click="openCreatePDFConfirm" class="btn btn-default" data-toggle="modal" data-target="#js-pdf-create-confirmation">PDFを作成する</button>
            </div>
            <div class="col-xs-2">
              <label>作成したPDFの履歴管理</label>
              <History data-intro="シートを作成した後は履歴に保存されます。過去につくったPDFやテーブルを復元することができます。" data-step="5" :labelmakerDb="labelmakerDb" :historyTable="historyTable" @restoreTableData="loadTableData"></History>
            </div>
            <div class="col-xs-2">
              <label>シートをリセット</label>
              <button type="button" class="btn btn-default" @click="openClearTableConfirm">リセットする</button>
            </div>
            <div class="col-xs-2">
              <label>ご意見ご要望</label>
              <button @click="goForm" type="button" class="btn btn-default">問い合わせる</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div id="table" class="hot-table" data-intro="このシートの各項目を記入してください。<br><br>コピペやドラッグなど、エクセルと同等の操作が可能です。<br><br>エクセルからもしくはエクセルへのコピペにも対応しています。" data-step="3">
    </div>
    <UserTerm></UserTerm>
    <Modal :options="createPDFAlert"></Modal>
    <Modal :options="tablesAlert"></Modal>
    <Modal :options="createPDFConfirm" @ok="createPDF"></Modal>
    <Modal :options="clearTableConfirm" @ok="clearTable"></Modal>
  </div>
</template>

<script>
global.jQuery = require('jquery');
const $ = global.jQuery;
require('bootstrap');
import _ from 'lodash';
import 'bootstrap/dist/css/bootstrap.css';
import UAParser from 'ua-parser-js';
import Handsontable from 'handsontable/dist/handsontable.full.js';
import 'handsontable/dist/handsontable.full.min.css';
import {
  introJs
} from 'intro.js';
import 'intro.js/introjs.css';
import lovefield from 'lovefield';
import letterpack from '../table_settings/letter-pack';
import UserTerm from './UserTerm.vue';
import History from './History.vue';
import Modal from './Modal.vue';
import formatter from '../utils/address-formatter'

//ハンズオンテーブル
let hot;

let tour = introJs();
tour.setOption('nextLabel', '次へ');
tour.setOption('prevLabel', '戻る');
tour.setOption('skipLabel', 'スキップ');
tour.setOption('doneLabel', 'PDFファイルのサンプルを見る');
tour.setOption('tooltipPosition', 'auto');
tour.setOption('positionPrecedence', ['left', 'right', 'bottom', 'top']);

export default {
  name: 'app',
  data() {
    return {
      maxPage: 50,
      schemaBuilder: null,
      labelmakerDb: null,
      historyTable: null,
      selected: 'letterpack',
      nowSetting: {
        columns: letterpack.columns,
        colHeaders: letterpack.colHeaders
      },
      pdfDatas: [],
      tablesAlert: {
        title: '',
        text: '',
        visible: false,
        confirm: false,
      },
      clearTableConfirm: {
        title: 'シートをリセット',
        text: '現在シートに記入している内容をすべて破棄しますか？',
        visible: false,
        confirm: true,
      },
      createPDFAlert: {
        title: '',
        text: '',
        visible: false,
        confirm: false,
      },
      createPDFConfirm: {
        title: 'シートからPDFを作成',
        text: '',
        visible: false,
        confirm: true,
      },
    }
  },
  computed: {
    apiEndPoint: function () {
      if (process.env.NODE_ENV === 'development') {
        return 'http://localhost:8080/api/v1/letterpack';
      } else {
        return './api/v1/letterpack';
      }
    }
  },
  methods: {
    startTutorial: function () {
      tour.start().oncomplete(function () {
        window.open('./assets/pdf/example.pdf?multipage=true', 'about:blank');
      });
    },
    initTable: function () {
      let self = this;
      let table = document.getElementById('table');
      hot = new Handsontable(table, {
        minSpareRows: self.maxPage,
        maxSpareRows: self.maxPage,
        height: $(window).height() - $('#menu').height() - 60,
        width: $('.container').width(),
        rowHeaders: true,
        comments: true,
        enterMoves: function (e) {
          let obj = {
            row: 0,
            col: 1
          }
          let colLength = self.nowSetting.columns.length;
          if (colLength === hot.getSelected()[1] + 1) {
            obj.row = 1;
            obj.col = (colLength - 1) * -1;
          }
          return obj;
        },
        afterValidate: function (isValid, value, row, prop, source) {
          if (isValid && _.includes(['toPost', 'fromPost'], prop)) {
            let query = "https://maps.googleapis.com/maps/api/geocode/json?address=" + value.replace(/-/g, '') + "&language=ja";
            $.get(query, function (data) {
              let result = data.results[0];
              if (result !== undefined) {
                let addresProp = (function () {
                  if (prop === 'toPost') {
                    return 'toAddres';
                  } else if (prop === 'fromPost') {
                    return 'fromAddres';
                  }
                }());
                if (_.isEmpty(hot.getDataAtRowProp(row, addresProp))) {
                  hot.setDataAtRowProp(row, addresProp, formatter.formatGoogleApisAddress(result));
                }
              } else { //住所の結果なし
                self.tablesAlert.text = '該当する郵便番号がありませんでした。'
                self.tablesAlert.title = '警告'
                self.tablesAlert.visible = true
              }
            });
          }
        }
      });
    },
    updateTableSettings: function () {
      if (!_.isNull(hot)) {
        let nowSetting = this.nowSetting;
        let cell = (function () {
          let arr = [];
          nowSetting.columns.forEach(function (column, index) {
            if (column.comment !== undefined) {
              let obj = {};
              obj.row = 0;
              obj.col = index;
              obj.comment = {
                value: column.comment
              }
              arr.push(obj)
            }
          });
          return arr;
        }());
        hot.updateSettings({
          columns: nowSetting.columns,
          colHeaders: nowSetting.colHeaders,
          cell: cell,
        });
      }
    },
    clearTable: function () {
      if (!_.isNull(hot)) {
        hot.destroy();
        this.initTable();
        this.updateTableSettings();
      }
      this.clearTableConfirm.visible = false;
    },
    getTableData: function () {
      let self = this;
      if (!_.isNull(hot)) {
        return _.slice(hot.getData(), 0, self.maxPage);
      }
    },
    loadTableData: function (data, template) {
      if (!_.isNull(hot)) {
        this.selected === template;
        this.selectTemplate();
        let keys = _.map(this.nowSetting.columns, 'data');
        let _data = []
        data.forEach(function (item) {
          let obj = {};
          keys.forEach(function (key, i) {
            obj[key] = item[i];
          })
          _data.push(obj);
        })
        hot.loadData(_data);
      }
    },
    selectTemplate: function () {
      //テンプレートの数だけここが増える
      if (this.selected === 'letterpack') {
        this.nowSetting.columns = letterpack.columns;
        this.nowSetting.colHeaders = letterpack.colHeaders;
      }
      this.updateTableSettings();
    },
    openClearTableConfirm: function () {
      this.clearTableConfirm.visible = true;
    },
    openCreatePDFConfirm: function () {
      let self = this;
      self.createPDFConfirm.text = `現在シートに記入している内容で<strong>${self.pdfDatas.length}ページ</strong>のPDFを作成します。<br><small>(未入力の項目がある行は無視されます。)</small><br><br> PDFを作成してよろしいですか？`,
        self.createPDFConfirm.visible = true;
      if (!_.isNull(hot)) {
        let datas = hot.getData();
        self.pdfDatas = [];
        datas.forEach(function (data) {
          let obj = {};
          if (!_.includes(data, null) && !_.includes(data, '')) {
            data.forEach(function (item, index) {
              obj[self.nowSetting.columns[index].data] = item;
            });
            self.pdfDatas.push(obj);
          }
        });
      }
    },
    createPDF: function () {
      let self = this;
      $('#js-pdf-create-confirmation').modal('hide');
      if (!_.isEmpty(self.pdfDatas)) {
        let datajson = JSON.stringify(self.pdfDatas);
        let xhr = new XMLHttpRequest();
        xhr.open('POST', self.apiEndPoint);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.responseType = 'arraybuffer';
        xhr.onprogress = function (ev) {
          self.createPDFAlert.visible = true
          let percent = Math.floor(parseInt(ev.loaded / ev.total * 10000) / 100);
          self.createPDFAlert.title = '処理中'
          self.createPDFAlert.text = 'PDFを作成しています。そのまましばらくお待ちください。<br><br><div class="progress"><div class="progress-bar" role="progressbar" style="width: ' + percent + '%;">' + percent + '%</div></div>';

        }
        xhr.onloadend = function () {
          self.createPDFAlert.visible = true
          if (this.status === 200) {
            let arrayBuffer = this.response;
            let blobUrl = window.URL.createObjectURL(new Blob([arrayBuffer], {
              type: 'application/pdf'
            }));
            self.createPDFAlert.title = '完了'
            self.createPDFAlert.text = 'PDFを作成しました。<a href="' + blobUrl + '" target="_blank">ファイルを開く</a><br><br><div class="progress"><div class="progress-bar" role="progressbar" style="width: 100%;">100%</div></div>'

            // ローカルDBに作成したデータを保存
            if (!_.isNull(self.labelmakerDb) && !_.isNull(self.historyTable)) {
              let row = self.historyTable.createRow(
                {
                  'templateType': self.selected,
                  'pdfData': arrayBuffer,
                  'tableData': self.getTableData(),
                  'createdAt': new Date(),
                }
              );
              self.labelmakerDb.insert().into(self.historyTable).values([row]).exec();
            }
          } else {
            self.createPDFAlert.title = 'エラー'
            self.createPDFAlert.text = 'エラーステータス：' + this.status + '<br><br>PDFの作成時にエラーが発生しました。<br>お手数ですが、しばらくたってからもう一度お試しください。'
          }

        };
        xhr.send(datajson);
      } else {
        self.createPDFAlert.title = 'エラー'
        self.createPDFAlert.text = 'PDFは作成できません。<br>作成するPDFが0ページです。未入力の項目はございませんか？<br><small>(未入力の項目がある行は無視されます。)</small>'
        self.createPDFAlert.visible = true
      }
    },
    goForm:function(){
      window.open('https://goo.gl/forms/Ne5RmXACzWpvaQRo2');
    }
  },
  created: function () {
    let self = this;
    // ローカルDB作成
    self.schemaBuilder = lf.schema.create('labelmaker', 1);

    self.schemaBuilder.createTable('history').
      addColumn('id', lf.Type.INTEGER).
      addColumn('templateType', lf.Type.STRING).
      addColumn('pdfData', lf.Type.ARRAY_BUFFER).
      addColumn('tableData', lf.Type.OBJECT).
      addColumn('createdAt', lf.Type.DATE_TIME).
      addPrimaryKey(['id'], true, lf.Order.DESC);

    self.schemaBuilder.connect().then(function (db) {
      self.labelmakerDb = db;
      self.historyTable = db.getSchema().table('history');
    })
  },
  mounted: function () {
    this.initTable();
    this.updateTableSettings();
    let parser = new UAParser();
    parser.setUA(window.navigator.userAgent);
    let result = parser.getResult();
    if (result.device.type === 'mobile' || result.device.type === 'tablet') {
      alert('本サービスはスマートフォン、タブレットでの動作を想定していません。快適に操作するためにはPCからアクセスしてください。');
    }
  },
  beforeUpdate: function () { },
  updated: function () { },
  components: {
    History,
    UserTerm,
    Modal
  }
}
</script>

<style lang="scss" scoped>
#app {
  #menu {
    margin: 10px auto;
    label {
      display: block;
    }
  }
  .handsontable td.htInvalid {
    background-color: #d9534f !important;
  }
}

.introjs-tooltip {
  max-width: 400px !important;
  min-width: 300px !important;
}

.hot-table {
  background-color: #eee;
  border: 1px solid #ccc;
  margin: 10px auto;
}
</style>
