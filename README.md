# maimaiBot

一个基于 [mirai](https://github.com/mamoe/mirai) 和 [舞萌 DX 查分器](https://www.diving-fish.com/maimaidx/prober) 编写的 maimai DX QQ 机器人插件。

## 支持的功能

* b40
* b50
* 查歌
* XXX 是什么歌
* 谱面详情（例：紫id11154）
* 随机歌曲（例：随个紫12+）
* 别名查询（例：11154有什么别名）
* 定数查歌
* 分数线
* 猜歌

## 部署南

[点我下载](https://github.com/xszqxszq/maimai-bot/releases/download/v1.1/maimai-bot-1.1.mirai.jar)

本插件开箱即用，只需和其他插件一样放入 [MCL](https://github.com/iTXTech/mcl-installer) 或其他版本的 Mirai 控制台 的 ```plugins``` 目录即可。

如果您尚不清楚 Mirai 如何安装，请阅读 [Mirai 官方教程](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md) ，在安装好控制台后再安装本插件使用。

## 常见问题

#### 如何修改字体？

修改 ```config/xyz.xszq.maimaiBot/config.yml``` 中的 ```fontName```值即可。

#### 如何更新歌曲别名？

本项目内置的别名来自 [歌曲别名添加表](https://docs.qq.com/sheet/DWGNNYUdTT01PY2N1) ，如果需要更新，则需要切换到“现有歌曲别名表”并点击右上角，导出为→csv并保存到 ```config/xyz.xszq.maimaiBot/aliases.csv```

#### 查不到任何歌曲谱面信息？

请检查 [舞萌 DX 查分器](https://www.diving-fish.com/maimaidx/prober) 是否可以访问，同时请检查您的网络连接是否畅通，是否开启了无效的代理设置等。