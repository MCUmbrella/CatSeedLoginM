# CatSeedLogin 猫种子登录
> 插件在Spigot API 1.13.2环境下开发的，
由于现在很多登录插件功能配置非常多，配置起来麻烦并且有很多用不到的功能。
crazylogin在高版本有各种匪夷所思的bug（总之我是被crazylogin从1.13.2的版本劝退自己开始造起了登录插件）
authme配置文件对一些经验不足的服主配置起来极其麻烦，甚至有人从入门到弃坑
有人测试1.7.10 和 1.11版本的服务器可以用 理论上应该支持1.7 ~ 1.18.1 一般都是低版本向上兼容。
## 基础功能

<details><summary>点我展开</summary><p>

*  注册 登录 修改密码 管理员设置密码
*  防止英文id大小写登录bug
*  登录前隐藏背包（需要ProtocolLib插件）
*  防止玩家登录之后被别人顶下线
*  下线之后指定时长内不能重新进入服务器（防止某些bug）
*  没有登录之前禁止移动,交互,攻击,发言,使用指令,传送,点击背包物品,丢弃物品,拾取物品
*  限制同ip的帐号同时在线/注册的数量
*  登录之前在配置文件指定的世界出生点,登录之后自动返回下线地点（可配置取消）
*  储存默认使用的是SQLite（也支持Mysql，需要配置文件sql.yml中配置打开）
*  密码加密储存,Crypt默认加密方式
*  进入游戏时游戏名的限制（由数字,字母和下划线组成 “可配置”长度的游戏名才能进入）
*  绑定邮箱，邮箱重置密码功能
*  支持bc端在没有登录时，禁止切换子服，登录后切换子服保持登录

</p></details>

## 下载
* 最新版 https://www.mcbbs.net/thread-847859-1-1.html
* 旧版 https://github.com/CatSeed/CatSeedLogin/tags
## 使用方式

<details><summary>点我展开</summary><p>

#### 如果是正常使用：
* 插件放入plugins文件夹重启服务器
#### 如果是配合BungeeCord连接多个子服使用：
* 插件放入作为登录服的那个子服plugins文件夹重启服务器，然后在plugins文件夹下找到CatSeedLogin文件夹修改bungeecord.yml中的配置，然后执行重载指令
* 复制一份插件再放入BungeeCord的plugins文件夹重启服务器，然后在plugins文件夹下找到CatSeedLogin-Bungee文件夹，修改bungeecord.yml中的配置，然后执行重载指令

</p></details>

## 指令

<details><summary>点我展开</summary><p>

### 登录
* `/login 密码`
* `/l 密码`
### 注册密码
* `/register 密码 重复密码`
* `/reg 密码 重复密码`
### 修改密码
* `/changepassword 旧密码 新密码 重复新密码`
* `/changepw 旧密码 新密码 重复新密码`
### 绑定邮箱
* `/bindemail set 邮箱`
* `/bdmail set 邮箱`
### 用邮箱收到的验证码完成绑定
* `/bindemail verify 验证码`
* `/bdmail verify 验证码`
### 忘记密码，请求服务器给自己绑定的邮箱发送重置密码的验证码
* `/resetpassword forget`
* `/repw forget`
### 用邮箱收到的验证码重置密码
* `/bindemail re 验证码 新密码`
* `/bdmail re 验证码 新密码`
### 管理指令
### 添加登录之前允许执行的指令 (支持正则表达式)
* `/catseedlogin commandWhitelistAdd 指令`
### 删除登录之前允许执行的指令 (支持正则表达式)
* `/catseedlogin commandWhitelistDel 指令`
### 查看登录之前允许执行的指令 (支持正则表达式)
* `/catseedlogin commandWhitelistInfo`
### 设置相同ip注册数量限制 （默认数量2）
* `/catseedlogin setMaxRegPerIP 数量`
### 设置相同ip登录数量限制 （默认数量2）
* `/catseedlogin setMaxOnlinePerIP 数量`
### 设置游戏名最小和最大长度 (默认最小是2 最大是15)
* `/catseedlogin setPlayerNameLength 最短 最长`
### 离开服务器重新进入间隔限制 单位：tick (1秒等于20tick) (默认60tick)
* `/catseedlogin setRejoinInterval 间隔`
### 设置玩家登录地点为你站着的位置 (默认登录地点为world世界的出生点)
* `/catseedlogin setSpawnLocation`
### 设置自动踢出未登录的玩家 (默认120秒，小于1秒则关闭此功能)
* `/catseedlogin setLoginTimeout 秒数`
### 打开/关闭 限制中文游戏名 (默认打开)
* `/catseedlogin forceStandardPlayerName`
### 打开/关闭 登录之前是否受到伤害 (默认登录之前不受到伤害)
* `/catseedlogin noDamageBeforeLogin`
### 打开/关闭 登录之后是否返回退出地点 (默认打开)
* `/catseedlogin backAfterLogin`
### 打开/关闭 登录之前是否强制在登录地点 (默认打开)
* `/catseedlogin noMoveBeforeLogin`
### 打开/关闭 死亡状态退出游戏记录退出位置 (默认打开)
* `/catseedlogin saveDeadPlayerLogoutLocation`
### 管理员强制删除账户
* `/catseedlogin delPlayer 玩家名`
### 管理员强制设置玩家密码
* `/catseedlogin setPwd 玩家名 密码`
### 重载配置文件
* `/catseedlogin reload`

</p></details>

## 权限
* `catseedlogin.command.catseedlogin`: 管理员指令/catseedlogin使用权限
## 配置文件
<details><summary>核心配置：settings.yml</summary><p>

插件也会在插件目录下生成一个叫settings.example.yml的带注释的示例配置文件供你参考。
```yaml
# 插件使用的语言。
language: "zh_CN"
# 每个IP地址可注册的账号数量。
maxRegPerIP: 2
# 同一个IP地址同时在线玩家的最大值。
maxOnlinePerIP: 2
# 只允许在玩家名中使用英文字母、数字和下划线？
# 设定为true时，将不允许玩家名中包含其它字符的玩家加入服务器。
forceStandardPlayerName: true
# 玩家名的最小长度。
minPlayerNameLength: 2
# 玩家名的最大长度。
maxPlayerNameLength: 15
# 如果设定为true，未登录玩家将不会受到伤害。
noDamageBeforeLogin: true
# 玩家在下线多少秒后可以重新进入服务器？
rejoinInterval: 60
# 如果设定为true，玩家登录成功后将会被传送到下线位置。
backAfterLogin: true
# 如果设定为true，未登录玩家将不能移动。
noMoveBeforeLogin: true
# 在登录之前可以使用的命令。
# 默认值适合大多数情况，支持正则表达式。
commandWhitelist:
  - /(?i)l(ogin)?(\z| .*)
  - /(?i)reg(ister)?(\z| .*)
  - /(?i)resetpassword?(\z| .*)
  - /(?i)repw?(\z| .*)
  - /(?i)worldedit cui
# 如果玩家一直没登录成功，多少秒后将会被踢出服务器？
loginTimeout: 120
# 如果设定为true，玩家死亡后不复活直接下线也会被保存下线位置。
saveDeadPlayerLogoutLocation: true
```

</p></details>

<details><summary>数据库配置：sql.yml</summary><p>

如果不使用mysql数据库储存，就请无视此配置  
```yaml
MySQL:  
# 是否开启数据库功能（false = 不开启）  
  Enable: false  
  Host: 127.0.0.1  
  Port: '3306'  
  Database: databaseName  
  User: root  
  Password: root
```

</p></details>

<details><summary>重置密码邮件配置：emailVerify.yml</summary><p>

如果不使用邮箱一系列功能，就请无视此配置  
```yaml
# 是否开启邮箱系列的功能（false = 不开启）  
Enable: false  
EmailAccount: "763737569@qq.com"  
EmailPassword: "123456"  
EmailSmtpHost: "smtp.qq.com"  
EmailSmtpPort: "465"  
SSLAuthVerify: true  
# 发件人的名字  
FromPersonal: "xxx服务器"
```

</p></details>

## 配合BungeeCord连接多个子服
插件可以在子服和bc端上运行，如果你是bc端连接多个子服的服务器架构，你需要在作为登录服的那个子服和bc端都装入这个插件，并设置bungeecord.yml配置文件

<details><summary>子服配置文件：bungeecord.yml</summary><p>

```yaml
# 是否开启bungeecord模式（false = 不开启）  
Enable: false  
# 设置IP（如果可以建议使用内网），会使用这个ip开启一个通讯服务与bc建立端通讯  
Host: 127.0.0.1  
# 设置端口  
Port: 2333  
# 验证密钥，类似设置密码一样，这里填写一串无法被人猜到无规律的字符（如果是内网可以不写）  
AuthKey: ""
```
</p></details>

<details><summary>bc端配置文件：bungeecord.yml</summary><p>

```yaml
# 设置IP，需要跟子服的一样（如果可以建议使用内网），从这个ip跟子服建立通讯  
Host: 127.0.0.1  
# 设置端口，需要跟子服一样  
Port: 2333  
# 作为登录服的服务器  
LoginServerName: "lobby"  
# 验证密钥，需要跟子服一样  
AuthKey: ""
```

</p></details>

### bc端指令
#### 重載bc端本插件的配置文件
`/CatSeedLoginBungee reload ` 或 `/cslb reload`

## 开发者部分

<details><summary>点我展开</summary><p>

### 事件
- CatSeedPlayerLoginEvent：玩家登录事件
  - `getPlayer()`: 获取触发事件的玩家的Player对象
  - `getResult()`: 获取登录操作的结果。登录成功返回`CatSeedPlayerLoginEvent.Result.SUCCESS`，失败返回`CatSeedPlayerLoginEvent.Result.FAIL`
- CatSeedPlayerRegisterEvent
  - `getPlayer()`: 获取触发事件的玩家的Player对象
### API
- CatSeedLoginAPI
  - `isLoggedIn(String)`: 从给定的玩家名判断玩家是否已登录
  - `isRegistered(String)`: 从给定的玩家名判断玩家是否已注册

</p></details>

## 联系
[点击进入 QQ交流群839815243](http://shang.qq.com/wpa/qunwpa?idkey=91199801a9406f659c7add6fb87b03ca071b199b36687c62a3ac51bec2f258a3)
