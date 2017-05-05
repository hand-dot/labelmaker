<template>
  <div id="app" class="container">
    <div id="menu" class="row">
      <div class="form-group">
        <label class="col-xs-6 control-label">
                テンプレートを選択し、住所などを下記のシートに記載してください。<br>
                入力が完了したら作成ボタンを押してください。<br>
                <u>*現在テンプレートはレターパックしかございません。</u>
              </label>
        <div class="col-xs-6">
          <div class="form-group">
            <div class="col-xs-6">
              <label>テンプレートを選択</label>
              <select name="template" v-model="selected"　@change="selectTemplate" class="form-control">
                      <option value="letterpack">レターパック</option>
                    </select>
            </div>
            <div class="col-xs-6">
              <label>シートからPDFを作成</label>
              <button type="button" @click="openCreatePDFModal" class="btn btn-default" data-toggle="modal" data-target="#confirmation">
                      PDF作成
                    </button>
                <div class="modal fade" id="confirmation" tabindex="-1" role="dialog" aria-labelledby="confirmationLabel">
                <div class="modal-dialog" role="document">
                  <div class="modal-content">
                    <div class="modal-header">
                      <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                      <h4 class="modal-title" id="confirmationLabel">シートからPDFを作成</h4>
                    </div>
                    <div class="modal-body">
                      現在シートに記入している内容で<strong>{{pdfDatas.length}}ページ</strong>のPDFを作成します。<br>
                      <small>(未入力の項目がある行は無視されます。)</small><br><br>
                      PDFを作成してよろしいですか？
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
    <div id="table">
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
  let hot;
  export default {
    name: 'app',
    data() {
      return {
        selected: 'letterpack',
        nowSetting: {
            columns: letterpack.columns,
            colHeaders: letterpack.colHeaders         
        },
        pdfDatas:[],
        smallModalText:''
      }
    },
    methods: {
      updateTableSettings: function(){
        if(!_.isNull(hot)){
          hot.updateSettings({
            columns: this.nowSetting.columns,
            colHeaders: this.nowSetting.colHeaders
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
      openCreatePDFModal: function(){
        let self = this;
          if(!_.isNull(hot)){
            let datas = hot.getData();
            self.pdfDatas = [];
            datas.forEach(function(data){
              let obj = {};
              if(!_.includes(data, null) && !_.includes(data, '')){
                data.forEach(function(item,index){
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
        if(!_.isEmpty(self.pdfDatas)){
          let childWindow = window.open('about:blank');
          let datajson=JSON.stringify(self.pdfDatas);
          let xhr = new XMLHttpRequest();
          xhr.open('POST', '/api/v1/letterpack');
          xhr.setRequestHeader('Content-Type', 'application/json');
          xhr.responseType = 'arraybuffer';
          xhr.onload = function() {
			      let arrayBuffer = this.response;
            let blob_url = window.URL.createObjectURL(new Blob([arrayBuffer], {type: 'application/pdf'}));
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
        }else{
          self.smallModalText = '作成するPDFが0ページです。<br>未入力の項目はございませんか？'
          $('#small-modal').modal('show');
        }
      },
      getUniqueStr:function(myStrong){
        let strong = 1000;
        if (myStrong) strong = myStrong;
        return new Date().getTime().toString(16)  + Math.floor(strong*Math.random()).toString(16);
      }
    },
    created: function() {},
    mounted: function() {
      let self = this;
      let table = document.getElementById('table');
  
      hot = new Handsontable(table, {
        minSpareRows: 50,
        height: $(window).height() - $('#menu').height() - 40,
        rowHeaders: true,
        enterMoves: function(e){
           let obj = {
            row: 0,
            col: 1,
          }
          let colLength = self.nowSetting.columns.length;
          if(colLength === hot.getSelected()[1] + 1){
            obj.row = 1; 
            obj.col = (colLength - 1) * -1; 
          }
          return obj;
        }
      });

      this.updateTableSettings();           
    },
    beforeUpdate: function() {},
    updated: function() {}
  }
</script>

<style lang="scss">
  #menu {
    margin-top: 20px;
    margin-bottom: 20px;
    label {
      display: block;
    }
  }
</style>
