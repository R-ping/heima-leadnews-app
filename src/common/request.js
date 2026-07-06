import axios from 'axios'
import crypto from 'crypto-js'
import store from '@/stores/store'

function Request() {
    this.store = null;
}
Request.prototype = {
    setStore: function (store) {
        this.store = store
    },
    postByEquipmentId: function (url, body) {
        var _this = this
        return this.store.getEquipmentId().then(function (equipmentId) {
            body['equipment_id'] = equipmentId
            return _this.post(url, body)
        }).catch(function (e) {
            return Promise.reject(e)
        })
    },
    getByEquipmentId: function (url, body) {
        var _this = this
        return this.store.getEquipmentId().then(function (equipmentId) {
            body['equipmentId'] = equipmentId
            return _this.get(url, body)
        }).catch(function (e) {
            return Promise.reject(e)
        })
    },
    post: function (path, body, parms) {
        var _this = this
        var time = new Date().getTime()
        if (parms == undefined) parms = {}
        else {
            path = path + '?' + objToQueryString(parms)
        }
        parms['t'] = time
        return this.store.getToken().then(function (token) {
            return _this.__fetch('POST', path, token, time, parms, body)
        }).catch(function (e) {
            if (e && e.status) {
                return Promise.reject(e)
            } else {
                return _this.__fetch('POST', path, '', time, parms, body)
            }
        })
    },
    get: function (path, parms) {
        var _this = this
        if (parms) {
            if (path.indexOf('?') == -1) {
                path += '?' + objToQueryString(parms)
            } else {
                path += '&' + objToQueryString(parms)
            }
        }
        var time = new Date().getTime()
        parms['t'] = time
        return this.store.getToken().then(function (token) {
            return _this.__fetch('GET', path, token, time, parms)
        }).catch(function (e) {
            if (e && e.status) {
                return Promise.reject(e)
            } else {
                return _this.__fetch('GET', path, '', time, parms)
            }
        })
    },
    __fetch: function (type, path, token, time, parms, body) {
        var headers = {
            'Content-Type': 'application/json; charset=UTF-8',
            'token': token,
            't': '' + time,
            'md': this.sign(parms)
        }
        var config = {
            method: type,
            url: path,
            headers: headers
        }
        if (body) {
            config.data = body
        }
        return axios(config).then(function (response) {
            return response.data
        }).catch(function (error) {
            if (error.response) {
                return Promise.reject(error.response)
            }
            return Promise.reject(error)
        })
    },
    sign: function (parms) {
        var arr = []
        for (var key in parms) {
            arr.push(key)
        }
        arr.sort()
        var str = ''
        for (var i in arr) {
            if (str != '') {
                str += '&'
            }
            str += arr[i] + '=' + parms[arr[i]]
        }
        return crypto.MD5(str).toString()
    }
}

function objToQueryString(obj) {
    var parts = []
    for (var key in obj) {
        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(obj[key]))
    }
    return parts.join('&')
}

export default new Request()
