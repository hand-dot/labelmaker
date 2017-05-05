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
              <button data-intro="このシートへの記入が終了したらこのボタンを押してPDFをダウンロードしてください。" data-step="3" type="button" @click="openCreatePDFModal" class="btn btn-default" data-toggle="modal" data-target="#js-pdf-create-confirmation">PDFを作成する</button>
              <div class="modal fade" id="js-pdf-create-confirmation" tabindex="-1" role="dialog" aria-labelledby="confirmationLabel">
                <div class="modal-dialog" role="document">
                  <div class="modal-content">
                    <div class="modal-header">
                      <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                      <h4 class="modal-title" id="confirmationLabel">シートからPDFを作成</h4>
                    </div>
                    <div class="modal-body">
                      現在シートに記入している内容で<strong>{{pdfDatas.length}}ページ</strong>のPDFを作成します。<br>
                      <small>(未入力の項目がある行は無視されます。)</small><br><br> PDFを作成してよろしいですか？
                    </div>
                    <div class="modal-footer">
                      <button type="button" class="btn btn-default" data-dismiss="modal">いいえ</button>
                      <button @click="createPDF" type="button" class="btn btn-primary">はい</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="text-center col-xs-4">
              <label>シートをリセット</label>
              <button type="button" class="btn btn-default" data-toggle="modal" data-target="#js-clear-sheet">リセットする</button>
              <div class="modal fade" id="js-clear-sheet" tabindex="-1" role="dialog" aria-labelledby="clearSheetLabel">
                <div class="modal-dialog" role="document">
                  <div class="modal-content">
                    <div class="modal-header">
                      <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                      <h4 class="modal-title" id="clearSheetLabel">シートをリセット</h4>
                    </div>
                    <div class="modal-body">
                      現在シートに記入している内容をすべて破棄しますか？
                    </div>
                    <div class="modal-footer">
                      <button type="button" class="btn btn-default" data-dismiss="modal">いいえ</button>
                      <button @click="clearSheet" type="button" class="btn btn-primary">はい</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div id="alert" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
              <div class="modal-dialog" role="document">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h5 class="modal-title" id="confirmationLabel">メッセージ</h5>
                  </div>
                  <div class="modal-body" v-html="smallModalText">{{smallModalText}}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div data-intro="このシートの各項目を記入してください。<br><br>コピペやドラッグなど、エクセルと同等の操作が可能です。<br><br>エクセルからもしくはエクセルへのコピペにも対応しています。" data-step="2" id="table">
    </div>
    <UserTerm></UserTerm>
  </div>
</template>

<script>
  global.jQuery = require('jquery');
  const $ = global.jQuery;
  require('bootstrap');
  import _ from 'lodash';
  import 'bootstrap/dist/css/bootstrap.css';
  import Handsontable from 'handsontable/dist/handsontable.full.js';
  import 'handsontable/dist/handsontable.full.min.css';
  import letterpack from './table_settings/letter-pack';
  import {introJs}  from 'intro.js';
  import 'intro.js/introjs.css';
  import UserTerm from './UserTerm.vue';
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
        smallModalText: '',
      }
    },
    methods: {
      startTutorial:function(){
        tour.start().oncomplete(function() {
          window.open('example.pdf?multipage=true','about:blank');
        });
      },
      initTable:function(){
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
          afterValidate:function(isValid,value,row,prop,source){
            if(isValid && _.includes(['toPost','fromPost'],prop)){
              let query = "https://maps.googleapis.com/maps/api/geocode/json?address="+value+"&language=ja";
              $.get(query,function(data){
                let result = data.results[0];
                if(result !== undefined){
                  let addressArr = result.address_components;
                  let addresProp = (function(){
                    if(prop === 'toPost'){
                      return 'toAddres';
                    }else{
                      return 'fromAddres';
                    }
                  }());
                  if(_.isEmpty(hot.getDataAtRowProp(row, addresProp))){
                    hot.setDataAtRowProp(row, addresProp, addressArr[3].long_name+addressArr[2].long_name+addressArr[1].long_name);
                  }
                }else{//住所の結果なし
                  self.smallModalText = '該当する郵便番号がありませんでした。'
                  $('#alert').modal('show');
                }
              });
            }
          }
        });
      },
      updateTableSettings: function() {
        if (!_.isNull(hot)) {
          let nowSetting = this.nowSetting;
          let cell = (function(){
            let arr = [];
            nowSetting.columns.forEach(function(column,index){
              if(column.comment !== undefined){
                let obj = {};
                obj.row = 0;
                obj.col = index;
                obj.comment = {value: column.comment}
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
      clearSheet: function(){
        if (!_.isNull(hot)) {
          hot.destroy();
          this.initTable();
          this.updateTableSettings();
        }
        $('#js-clear-sheet').modal('hide');
      },
      selectTemplate: function() {
        //テンプレートの数だけここが増える
        if (this.selected === 'letterpack') {
          this.nowSetting.columns = letterpack.columns;
          this.nowSetting.colHeaders = letterpack.colHeaders;
        }
        this.updateTableSettings();
      },
      openCreatePDFModal: function() {
        let self = this;
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
        self.smallModalText = '';
        $('#js-pdf-create-confirmation').modal('hide');
        if (!_.isEmpty(self.pdfDatas)) {
          let childWindow = window.open('about:blank');
          let datajson = JSON.stringify(self.pdfDatas);
          let xhr = new XMLHttpRequest();
          xhr.open('POST', '/api/v1/letterpack');
          xhr.setRequestHeader('Content-Type', 'application/json');
          xhr.responseType = 'arraybuffer';
          xhr.onloadend = function() {
            if(this.status === 200){
              let arrayBuffer = this.response;
              let blob_url = window.URL.createObjectURL(new Blob([arrayBuffer], {type: 'application/pdf'}));
              childWindow.location.href = blob_url;
              self.smallModalText = 'PDFを作成しました。'
            }else{
              childWindow.close();
              self.smallModalText = 'エラーステータス：'+this.status+'<br><br>PDFの作成時にエラーが発生しました。<br>お手数ですが、しばらくたってからもう一度お試しください。'
            }
            $('#alert').modal('show');
            childWindow = null;
          };
          xhr.send(datajson);
        } else {
          self.smallModalText = 'PDFは作成できませんでした。<br>作成するPDFが0ページです。未入力の項目はございませんか？<br><small>(未入力の項目がある行は無視されます。)</small>'
          $('#alert').modal('show');
        }
      }
    },
    created: function() {},
    mounted: function() {
      this.initTable();
      this.updateTableSettings();
    },
    beforeUpdate: function() {},
    updated: function() {},
    components:{
      UserTerm
    }
  }
</script>

<style lang="scss">
  #app {
    .black{
      color:#000;
    }
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
  .introjs-tooltip{
    max-width: 400px !important;
    min-width: 300px !important;
  }
</style>
