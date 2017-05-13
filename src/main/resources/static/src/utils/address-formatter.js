export default {
    /**
     * グーグルの郵便番号APIの返り値を整えて文字列で返します。
     */
    formatGoogleApisAddress: function(result) {
        let addressArr = result.address_components;
        addressArr.pop();
        addressArr.shift();
        let str = '';
        _.reverse(addressArr).forEach(function(address) {
            str += address.long_name;
        });
        return str;
    }
}