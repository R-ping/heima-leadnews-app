import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

function Cache(){
    this.tokenKey = "TOKEN_KEY"
    this.equipmentidKey = "EQUIPMENTID_KEY"
    // 设置默认设备ID
    if(!localStorage.getItem(this.equipmentidKey)){
        localStorage.setItem(this.equipmentidKey, "8D3E8E0CF883C4E99329AF8A29300AB6")
    }
}
Cache.prototype={
    setToken : function(token){
        return this.__setItem(this.tokenKey,token);
    },
    getToken : function(){
        return this.__getItem(this.tokenKey);
    },
    setEquipmentId : function(equipmentId){
        return this.__setItem(this.equipmentidKey,equipmentId);
    },
    getEquipmentId : function(){
        return this.__getItem(this.equipmentidKey);
    },
    clearToken : function(){
        return this.__removeItem(this.tokenKey);
    },
    __setItem : function(key,value){
        try {
            localStorage.setItem(key, value);
            return Promise.resolve(true);
        } catch(e) {
            return Promise.reject(false);
        }
    },
    __getItem : function(key){
        try {
            let val = localStorage.getItem(key);
            if(val !== null && val !== undefined){
                return Promise.resolve(val);
            } else {
                return Promise.reject({result:'fail', data:null});
            }
        } catch(e) {
            return Promise.reject(e);
        }
    },
    __removeItem : function(key){
        try {
            localStorage.removeItem(key);
            return Promise.resolve(true);
        } catch(e) {
            return Promise.reject(false);
        }
    }
}
const cache = new Cache();

const store = new Vuex.Store({
    state: {
        token: '',
        showLoginModal: false
    },
    mutations: {
        SET_TOKEN(state, token) {
            state.token = token
            cache.setToken(token)
        },
        CLEAR_TOKEN(state) {
            state.token = ''
            cache.clearToken()
        },
        SHOW_LOGIN_MODAL(state) {
            state.showLoginModal = true
        },
        HIDE_LOGIN_MODAL(state) {
            state.showLoginModal = false
        }
    },
    actions: {
        login({ commit }, token) {
            commit('SET_TOKEN', token)
        },
        logout({ commit }) {
            commit('CLEAR_TOKEN')
        },
        showLogin({ commit }) {
            commit('SHOW_LOGIN_MODAL')
        },
        hideLogin({ commit }) {
            commit('HIDE_LOGIN_MODAL')
        }
    },
    getters: {
        isLoggedIn: state => !!state.token,
        showLoginModal: state => state.showLoginModal
    }
})

// 初始化时从localStorage恢复token
cache.getToken().then(token => {
    if (token) {
        store.commit('SET_TOKEN', token)
    }
}).catch(() => {})

// 兼容旧的$store.setToken/getToken调用
store.setToken = function(token) {
    store.commit('SET_TOKEN', token)
}
store.getToken = function() {
    return cache.getToken()
}
store.clearToken = function() {
    store.commit('CLEAR_TOKEN')
}

export default store