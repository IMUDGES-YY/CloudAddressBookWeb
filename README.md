# CloudAddressBookWeb
云通讯录

<br>
目前服务器地址为：183.175.12.168:9998

关于服务器是否启用，需要联系管理员。

# 服务器接口
## 返回码含义:
| 返回码        | 含义   |
| :----: | :----:  |
|  0     | 登陆成功  |
| -1     | 参数错误   |
| -2     | 处理超时   |
| -3     | 用户名或密码错误   |
| -4     | 登录状态无效   |
| -5     | 短信验证码错误或者已经失效|
| -6     | 用户已存在|
| -7     | 发送验证码太频繁|
| -8     | 旧密码错误|

### 登录
#### 地址：/login.php
#### 请求参数：username=[username or phoneNum]&password=[passowrd]&imei=[imei]&ts=[ts]
#### 成功返回样例：
```json
{
    "ret":0,
    "msg":"登陆成功",
    "data":{
        "Id":1,
        "username":"admin",
        "phone":"18647705052",
        "name":null,
        "ak":"0f2760f790ff71c3f48f995cb0759e0ef7d559f75bd16a1c650da6b17e0ea1380e4a7a35756c7a63d344727ab1cfa068",
        "userPhoto":null
    }
}
```
#### 失败返回样例：
```json
{
    "ret":-1,
    "msg":"参数错误",
    "data":null
}
```

<br>


### 查询用户登录状态
#### 地址：/check_login_status.php
#### 请求参数：ak=[ak]
#### 成功返回样例：
```json
{
    "ret":"0",
    "msg":"ok",
    "data":{
        "nickName":null,
        "phoneNum":"18647705052",
        "ak":"324239508d5572b3c5e1634a603932445ab9e17103121d562e0d2da85f7bdb1a3e3f9c3b4ee904e10e1aa1cb27f25cd9",
        "imei":null,
        "ip":null,
        "email":"yangyang@imudges.com",
        "sex":1
    }
}
```
#### 失败返回样例：
```json
{
    "ret":"-4",
    "msg":"登录状态无效",
    "data":null
}
```
<br>

### 通过手机号注册
#### 地址：/register_by_phone
#### 请求参数：phone=[phone]&code=[code]&password=[password]
#### *请在客户端判断手机号合法性，当然服务器也会判断*
#### 成功返回样例：
```json
{
    "ret":"0",
    "msg":"ok",
    "data":{
        "nickName":null,
        "phone":"18647705052",
        "ak":"603c103e05ed4a45846201839cc1026034238e1289194dd0b14d3132852e493e7c0c6d992e02cbeb152935e0b83cb6e4",
        "imei":null,
        "ip":null,
        "email":null,
        "sex":0
    }
}
```
#### 失败返回样例：
```json
{
    "ret":"-5",
    "msg":"短信验证码错误或者已经失效",
    "data":null
}
```
<br>

### 发送短信
#### 地址：/send_sms
#### 请求参数：phone=[phone]
#### *请在客户端判断手机号合法性，当然服务器也会判断*
#### 成功返回样例：
```json
{
    "ret":"0",
    "msg":"ok"
}
```
#### 失败返回样例：
```json
{
    "ret":"-7",
    "msg":"发送验证码太频繁"
}
```
<br>

### 验证验证码
#### 地址：/check_sms
#### 请求参数：phone=[phone]&code=[code]
#### *请在客户端判断手机号合法性，当然服务器也会判断*
#### 成功返回样例：
```json
{
    "ret":"0",
    "msg":"ok"
}
```
#### 失败返回样例：
```json
{
    "ret":"-5",
    "msg":"短信验证码错误或者已经失效"
}
```
<br>

### 忘记密码
#### 地址：/change_password_by_code
#### 请求参数：phone=[phone]&code=[code]&password=[password]
#### *请在客户端判断手机号合法性，当然服务器也会判断*
#### 成功返回样例：
```json
{
    "ret":"0",
    "msg":"ok"
}
```
#### 失败返回样例：
```json
{
    "ret":"-5",
    "msg":"短信验证码错误或者已经失效"
}
```
<br>