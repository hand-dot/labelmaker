<template>
    <div :id="id" class="modal fade" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content text-center">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="confirmationLabel">
                        <strong>{{options.title}}</strong>
                    </h4>
                </div>
                <div class="modal-body" v-html="options.text">{{options.text}}</div>
                <div class="modal-footer" v-if="options.confirm">
                    <button type="button" class="btn btn-default" data-dismiss="modal" v-on:click.prevent="cancel">いいえ</button>
                    <button type="button" class="btn btn-primary" v-on:click.prevent="ok">はい</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
global.jQuery = require('jquery');
const $ = global.jQuery;
require('bootstrap');
import 'bootstrap/dist/css/bootstrap.css';
import uuid from 'uuid';

export default {
    name: 'modal',
    data() {
        return {
            id: null
        }
    },
    props: {
        options: Object
        //optionsのプロパティは下記です。 
        // options.title: '',
        // options.text: '',
        // options.visible:false,
        // options.confirm:false,
    },
    methods: {
        open: function () {
            let self = this;
            $(`#${this.id}`).on('hidden.bs.modal', function () {
                self.options.visible = false; // ESCで閉じたときにこれが必要となる
            }).modal('show');
        },
        close: function () {
            $(`#${this.id}`).modal('hide');
        },
        ok: function () {
            this.options.visible = false;
            this.$emit('ok');
        },
        cancel: function () {
            this.options.visible = false;
            this.$emit('cancel');
        }
    },
    created: function () {
        this.id = `message-${uuid()}`;
    },
    watch: {
        'options.visible': function () {
            if (this.options.visible) {
                this.open();
            } else {
                this.close();
            }
        }
    },
    mounted: function () { },
    beforeUpdate: function () { },
    updated: function () { },
    components: {}
}
</script>

<style lang="scss" scoped>

</style>
