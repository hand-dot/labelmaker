<template>
  <div id="app" class="container">
    <div id="menu" class="row">
      <div class="form-group">
        <label class="col-xs-6 control-label">
          レターパックなどの記入を効率的に行うことができるサービスです。<br>エクセルの操作感で一度に最大50件のラベルを作成できます。
          <button @click="startTutorial"　type="button" class="btn btn-default">使い方を見る</button>
        </label>
        <div class="col-xs-6">
          <div class="form-group">
            <div class="col-xs-6">
              <label>テンプレートを選択</label>
              <select data-intro="作成したいテンプレートを選択してください。<br><br>(現在レターパックのみ)" data-step="1" name="template" v-model="selected" 　@change="selectTemplate" class="form-control">
                <option value="letterpack">レターパック</option>
              </select>
            </div>
            <div class="col-xs-6">
              <label>シートからPDFを作成</label>
              <button data-intro="このシートへの記入が終了したらこのボタンを押してPDFをダウンロードしてください。" data-step="3" type="button" @click="openCreatePDFModal" class="btn btn-default" data-toggle="modal" data-target="#confirmation">PDF作成</button>
              <div class="modal fade" id="confirmation" tabindex="-1" role="dialog" aria-labelledby="confirmationLabel">
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
            <div id="small-modal" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
              <div class="modal-dialog modal-sm" role="document">
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
  </div>
</template>

<script>
  global.jQuery = require('jquery');
  const $ = global.jQuery;
  require('bootstrap');
  import _ from 'lodash'
  import 'bootstrap/dist/css/bootstrap.css'
  import Handsontable from 'handsontable/dist/handsontable.full.js'
  import 'handsontable/dist/handsontable.full.min.css'
  import letterpack from './table_settings/letter-pack'
  import {introJs}  from 'intro.js';
  import 'intro.js/introjs.css';
  let hot;
  let tour = introJs();
  tour.setOption('nextLabel', '次へ')
  tour.setOption('prevLabel', '戻る')
  tour.setOption('skipLabel', 'スキップ')
  tour.setOption('doneLabel', 'PDFファイルのサンプルを見る')
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
        smallModalText: ''
      }
    },
    methods: {
      startTutorial:function(){
        tour.start().oncomplete(function() {
          window.open('example.pdf?multipage=true','about:blank');
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
        $('#confirmation').modal('hide');
        if (!_.isEmpty(self.pdfDatas)) {
          let childWindow = window.open('about:blank');
          let datajson = JSON.stringify(self.pdfDatas);
          let xhr = new XMLHttpRequest();
          xhr.open('POST', '/api/v1/letterpack');
          xhr.setRequestHeader('Content-Type', 'application/json');
          xhr.responseType = 'arraybuffer';
          xhr.onload = function() {
            let arrayBuffer = this.response;
            let blob_url = window.URL.createObjectURL(new Blob([arrayBuffer], {
            type: 'application/pdf'
            }));
            childWindow.location.href = blob_url;
            childWindow = null;
            self.smallModalText = 'PDFを作成しました。'
            $('#small-modal').modal('show');
          };
          xhr.onerror = function() {
            childWindow.close();
            childWindow = null;
            self.smallModalText = 'PDFの作成時にエラーが発生しました。<br>お手数ですが、しばらくたってからもう一度お試しください。'
            $('#small-modal').modal('show');
          };
          xhr.send(datajson);
        } else {
          self.smallModalText = '作成するPDFが0ページです。<br>未入力の項目はございませんか？'
          $('#small-modal').modal('show');
        }
      }
    },
    created: function() {},
    mounted: function() {
      let self = this;
      let table = document.getElementById('table');
  
      hot = new Handsontable(table, {
        minSpareRows: 50,
        maxSpareRows: 50,
        height: $(window).height() - $('#menu').height() - 40,
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
                $('#small-modal').modal('show');
              }
            });
          }
        }
      });

      this.updateTableSettings();
    },
    beforeUpdate: function() {},
    updated: function() {}
  }
</script>

<style lang="scss">
  #app {
    #menu {
      margin-top: 20px;
      margin-bottom: 20px;
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
