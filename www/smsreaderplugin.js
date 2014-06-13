var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var smsreaderplugin = {
getUnreadSmsMessages:function (successCallback, failureCallback) {    
    exec(successCallback, failureCallback, 'SmsReaderPlugin', 'GET_UNREAD_SMS_MESSAGES', []);
},
//
setSmsMessageRead:function(successCallback,failureCallback, messageId) {
    exec(successCallback, failureCallback, 'SmsReaderPlugin', 'SET_SMS_MESSAGE_READ', [messageId]);
},
//
setSmsMessageUnread:function(successCallback,failureCallback, messageId) {
    exec(successCallback, failureCallback, 'SmsReaderPlugin', 'SET_SMS_MESSAGE_UNREAD', [messageId]);
},
//
deleteSmsMessage:function(successCallback,failureCallback, messageId) {
    exec(successCallback, failureCallback, 'SmsReaderPlugin', 'DELETE_SMS_MESSAGE', [messageId]);
},
};

module.exports=smsreaderplugin;
