<template>
  <div id="app" class="container">
    <div id="menu" class="row">
      <div class="form-group">
        <div class="col-xs-6 control-label">
          <strong style="line-height: 1.8">レターパックなどの記入を効率的に行うためのサービスです。<br>エクセルの操作感で一度に最大50件のラベルを作成できます。</strong style="line-height: 1.8">
          <button @click="startTutorial" type="button" class="btn btn-default">使い方を見る</button>
        </div>
        <div class="col-xs-6 text-center">
          <div class="form-group">
            <div class="text-center col-xs-4">
              <label>テンプレートを選択</label>
              <select data-intro="作成したいテンプレートを選択してください。<br><br>(現在レターパックのみ)" data-step="1" name="template" v-model="selected" 　@change="selectTemplate" class="form-control">
                <option value="letterpack">レターパック</option>
              </select>
            </div>
            <div class="text-center col-xs-4">
              <label>シートからPDFを作成</label>
              <button data-intro="このシートへの記入が終了したらこのボタンを押してPDFをダウンロードしてください。" data-step="3" type="button" @click="openCreatePDFConfirm" class="btn btn-default" data-toggle="modal" data-target="#js-pdf-create-confirmation">PDFを作成する</button>
            </div>
            <div class="text-center col-xs-4">
              <label>シートをリセット</label>
              <button type="button" class="btn btn-default" @click="openClearTableConfirm">リセットする</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div data-intro="このシートの各項目を記入してください。<br><br>コピペやドラッグなど、エクセルと同等の操作が可能です。<br><br>エクセルからもしくはエクセルへのコピペにも対応しています。" data-step="2" id="table">
    </div>
    <UserTerm></UserTerm>
    <Modal :options="createPDFAlert"></Modal>
    <Modal :options="tablesAlert"></Modal>
    <Modal :options="createPDFConfirm" v-on:ok="createPDF"></Modal>
    <Modal :options="clearTableConfirm" v-on:ok="clearTable"></Modal>
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
  import letterpack from '../table_settings/letter-pack';
  import UserTerm from './UserTerm.vue';
  import Modal from './Modal.vue';
  import formatter from '../utils/address-formatter'
  
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
    methods: {
      startTutorial: function() {
        tour.start().oncomplete(function() {
          window.open('./assets/pdf/example.pdf?multipage=true', 'about:blank');
        });
      },
      initTable: function() {
        let self = this;
        let table = document.getElementById('table');
        hot = new Handsontable(table, {
          minSpareRows: 50,
          maxSpareRows: 50,
          height: $(window).height() - $('#menu').height() - 60,
          rowHeaders: true,
          comments: true,
          enterMoves: function(e) {
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
          afterValidate: function(isValid, value, row, prop, source) {
            if (isValid && _.includes(['toPost', 'fromPost'], prop)) {
              let query = "https://maps.googleapis.com/maps/api/geocode/json?address=" + value.replace(/-/g, '') + "&language=ja";
              $.get(query, function(data) {
                let result = data.results[0];
                if (result !== undefined) {
                  let addresProp = (function() {
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
      updateTableSettings: function() {
        if (!_.isNull(hot)) {
          let nowSetting = this.nowSetting;
          let cell = (function() {
            let arr = [];
            nowSetting.columns.forEach(function(column, index) {
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
      clearTable: function() {
        if (!_.isNull(hot)) {
          hot.destroy();
          this.initTable();
          this.updateTableSettings();
        }
        this.clearTableConfirm.visible = false;
      },
      selectTemplate: function() {
        //テンプレートの数だけここが増える
        if (this.selected === 'letterpack') {
          this.nowSetting.columns = letterpack.columns;
          this.nowSetting.colHeaders = letterpack.colHeaders;
        }
        this.updateTableSettings();
      },
      openClearTableConfirm: function() {
        this.clearTableConfirm.visible = true;
      },
      openCreatePDFConfirm: function() {
        let self = this;
        self.createPDFConfirm.text = `現在シートに記入している内容で<strong>${self.pdfDatas.length}ページ</strong>のPDFを作成します。<br><small>(未入力の項目がある行は無視されます。)</small><br><br> PDFを作成してよろしいですか？`,
          self.createPDFConfirm.visible = true;
        if (!_.isNull(hot)) {
          let datas = hot.getData();
          self.pdfDatas = [];
          datas.forEach(function(data) {
            let obj = {};
            if (!_.includes(data, null) && !_.includes(data, '')) {
              data.forEach(function(item, index) {
                obj[self.nowSetting.columns[index].data] = item;
              });
              self.pdfDatas.push(obj);
            }
          });
        }
      },
      createPDF: function() {
        let self = this;
        $('#js-pdf-create-confirmation').modal('hide');
        if (!_.isEmpty(self.pdfDatas)) {
          let datajson = JSON.stringify(self.pdfDatas);
          let xhr = new XMLHttpRequest();
          xhr.open('POST', './api/v1/letterpack');
          xhr.setRequestHeader('Content-Type', 'application/json');
          xhr.responseType = 'arraybuffer';
          xhr.onprogress = function(ev) {
            self.createPDFAlert.visible = true
            let percent = Math.floor(parseInt(ev.loaded / ev.total * 10000) / 100);
            self.createPDFAlert.title = '処理中'
            self.createPDFAlert.text = 'PDFを作成しています。そのまましばらくお待ちください。<br><br><div class="progress"><div class="progress-bar" role="progressbar" style="width: ' + percent + '%;">' + percent + '%</div></div>';
  
          }
          xhr.onloadend = function() {
            self.createPDFAlert.visible = true
            if (this.status === 200) {
              let arrayBuffer = this.response;
              let blob_url = window.URL.createObjectURL(new Blob([arrayBuffer], {
                type: 'application/pdf'
              }));
              self.createPDFAlert.title = '完了'
              self.createPDFAlert.text = 'PDFを作成しました。<a href="' + blob_url + '" target="_blank">ファイルを開く</a><br><br><div class="progress"><div class="progress-bar" role="progressbar" style="width: 100%;">100%</div></div>'
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
      }
    },
    created: function() {},
    mounted: function() {
      this.initTable();
      this.updateTableSettings();
      let parser = new UAParser();
      parser.setUA(window.navigator.userAgent);
      let result = parser.getResult();
      if (result.device.type === 'mobile' || result.device.type === 'tablet') {
        alert('本サービスはスマートフォン、タブレットでの動作を想定していません。快適に操作するためにはPCからアクセスしてください。');
      }
    },
    beforeUpdate: function() {},
    updated: function() {},
    components: {
      UserTerm,
      Modal
    }
  }
</script>

<style lang="scss" scoped>
  #app {
    #menu {
      margin-top: 20px;
      margin-bottom: 10px;
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
</style>
