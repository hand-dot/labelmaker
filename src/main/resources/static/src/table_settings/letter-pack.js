export default {
    colHeaders: function(index) {
        let self = this;
        return self.columns[index].value;
    },
    columns: [{
            data: 'toPost',
            value: '[届け先]郵便番号',
        },
        {
            data: 'toAddres',
            value: '[届け先]おところ',
        },
        {
            data: 'toName',
            value: '[届け先]おなまえ',
        },
        {
            data: 'toTel',
            value: '[届け先]電話番号',
        },
        {
            data: 'fromPost',
            value: '[依頼主]郵便番号',
        },
        {
            data: 'fromAddres',
            value: '[依頼主]おところ',
        },
        {
            data: 'fromName',
            value: '[依頼主]おなまえ',
        },
        {
            data: 'fromTel',
            value: '[依頼主]電話番号',
        },
        {
            data: 'itemName',
            value: '品名'
        }
    ]
}