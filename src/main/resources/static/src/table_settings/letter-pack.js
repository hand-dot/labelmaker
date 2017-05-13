export default {
    colHeaders: function(index) {
        let self = this;
        return self.columns[index].value;
    },
    columns: [{
            data: 'toPost',
            value: '[To]郵便番号',
            validator: /^\d{3}[-]\d{4}$|^\d{7}$/,
            comment: '半角数字7桁で入力してください\n\n[To]おところが空欄の場合、\n郵便番号入力後に[To]おところを自動入力します。',
        },
        {
            data: 'toAddres',
            value: '[To]おところ',
        },
        {
            data: 'toName',
            value: '[To]おなまえ',
        },
        {
            data: 'toTel',
            value: '[To]電話番号',
            comment: '届け先の電話番号が不明の場合は\n半角数字で　0　を入力してください\n仕上がったPDFでは空欄になります。',
        },
        {
            data: 'fromPost',
            value: '[From]郵便番号',
            validator: /^\d{3}[-]\d{4}$|^\d{7}$/,
            comment: '半角数字7桁で入力してください\n\n[From]おところが空欄の場合、\n郵便番号入力後に[From]おところを自動入力します。',
        },
        {
            data: 'fromAddres',
            value: '[From]おところ',
        },
        {
            data: 'fromName',
            value: '[From]おなまえ',
        },
        {
            data: 'fromTel',
            value: '[From]電話番号',
        },
        {
            data: 'itemName',
            value: '品名'
        }
    ]
}