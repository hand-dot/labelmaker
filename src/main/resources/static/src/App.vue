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
                      <option value="letterPack">レターパック</option>
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
  import letterPack from './table_settings/letter-pack'
  let hot;
  export default {
    name: 'app',
    data() {
      return {
        selected: 'letterPack',
        nowSetting: {
            columns: letterPack.columns,
            colHeaders: letterPack.colHeaders         
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
          if (this.selected === 'letterPack') {
            this.nowSetting.columns = letterPack.columns;
            this.nowSetting.colHeaders = letterPack.colHeaders;
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
        this.smallModalText = '';
        console.log(this.pdfDatas);
        $('#confirmation').modal('hide');
        if(!_.isEmpty(this.pdfDatas)){
          $.ajax({
              type:'post',
              url:'/path/to/post',
              data:JSON.stringify(self.pdfDatas),
              contentType: 'application/json',
              dataType : 'text',
              contentType : 'application/pdf',
              success: function(response) {
                this.smallModalText = 'PDFを作成が完了しました。'
              },
              error: function() {
                this.smallModalText = 'PDFを作成に失敗しました。'
              },
              beforeSend: function(){
                this.smallModalText = 'PDFを作成中です。<br>そのままお待ちください。'
                $('#small-modal').modal('show');
              },
              complete: function() {
                $('#small-modal').modal('hide');
              }
          });
        }else{
          this.smallModalText = '作成するPDFが0ページです。<br>未入力の項目はございませんか？'
          $('#small-modal').modal('show');
        }

      }
    },
    created: function() {},
    mounted: function() {
      let table = document.getElementById('table');
  
      hot = new Handsontable(table, {
        minSpareRows: 50,
        height: $(window).height() - $('#menu').height() - 40,
        rowHeaders: true
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
