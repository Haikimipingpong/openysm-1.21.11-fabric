# OpenYSM 1.21.11 / Fabric —— 非官方移植（源码）

> ## 🔗 配套仓库：**[YSM x TaCZ Bridge →](https://github.com/Haikimipingpong/ysm-tacz-bridge)**
> 用官方的 `openysm-fabric-2.6.5.9` jar？装那个桥接模组就行（不改官方 jar）。
> 用本仓库这份移植源码构建？TaCZ 联动已经内置，**不要再装桥接模组**。

> 非官方项目，与 OpenYSM 上游、Yes Steve Model 官方及其作者没有隶属关系，也没有获得任何授权或背书。

[English](#english)

## 这是什么

[OpenYSM](https://github.com/OpenYSM/OpenYSM) 是 Yes Steve Model (YSM) 的开源替代实现：用 GeckoLib
加载 `.ysm` 模型与动画，替换玩家模型。本仓库是它面向 **Minecraft 1.21.11 + Fabric** 的一份可用源码。

## 这份代码是哪来的（请先读这一段）

- **OpenYSM 原项目只发布过 Minecraft 1.20.1 / Forge 版本**（Architectury 多平台结构，MIT）。
  上游**没有** 1.21.11 版本，也**没有** Fabric 版本。所以下面这份 1.21.11 + Fabric 的东西，
  不是上游作者做的。
- 本仓库的 1.21.11 / Fabric 版本，是整理者**在一份民间整合包（第三方整合包）里发现的**：
  随整合包分发的只有一个编译好的 `openysm-fabric-2.6.5.9-mc1.21.11.jar`，jar 里没有留下作者、
  没有仓库地址、没有联系方式。**这份移植的最初作者不明。**
- 因此：**1.21.11 / Fabric 移植部分的著作权属于那位未知的原作者**，本仓库不主张这部分权利。
  本仓库做的事是：把那份 jar 整理成可读、可重新构建的源码，修掉实际遇到的问题，并把来源写清楚。
- **如果你就是这份移植的作者**：请开一个 issue 说明，我们会把你的署名补到 README、NOTICE
  和 `fabric.mod.json` 的 `authors` 里；如果你不希望它被公开，说明后我们会立刻下架。
- 上游 OpenYSM 的 MIT 许可证与版权声明原样保留，见 [LICENSE.txt](LICENSE.txt) 与 [NOTICE](NOTICE)。

## 本仓库做了什么

拿到手时是一份「能跑但很脏」的源码树。整理过程中做的事：

**构建与平台**
- 把上游 `common` + `fabric` 两个源码集合并成单模块，去掉 Architectury 插件与 `@ExpectPlatform`
  运行时分发，只保留 Architectury API 作为普通依赖。
- 依赖对齐 1.21.11：Fabric API 0.141.6、Cardinal Components 7.3.1（`org.ladysnake.cca`）、
  Architectury API 19.0.1；构建脚本里清掉了上游遗留、实际已无人引用的两个 file 依赖。
- `gradle.properties` 里的版本号统一为 `2.6.6.6-port1`。

**1.21.11 适配（节选）**
- `ResourceLocation` → `Identifier`，构造方式改为 `Identifier.fromNamespaceAndPath` / `parse`。
- 实体渲染改为 render state 两段式（`createRenderState` / `extractRenderState` / `submit`）；
  玩家模型渲染改为 mixin `AvatarRenderer#submit` 接管，GeckoLib 的即时绘制走
  `OrderedSubmitNodeCollector#submitCustomGeometry`。
- 网络层改用 `CustomPacketPayload`（仍是原始字节壳，上游的分片协议不变）。
- `CompoundTag` 的 getter 改为 Optional / `getXxxOr`；GUI 输入改为事件对象
  （`MouseButtonEvent` 等）、`renderBackground`/`renderContents`/tooltip 延迟提交等一批控件 API 变更。
- Cardinal Components 组件接口改为 `readData(ValueInput)` / `writeData(ValueOutput)`。

**修掉的坑（真机上踩出来的）**
- 原生库改为**按内容哈希命名**（`libysm-core-<sha1前12位>.dylib`）。macOS 的 AMFI 按「路径 + mtime」
  缓存代码签名，换版本时原地覆盖同名 dylib 会被内核判定签名失效，整个 JVM 直接 `SIGKILL`
  ——没有崩溃报告，日志停在 `Begin load native library`。按哈希分文件后不再互相覆盖。
- 上游的 SIMD 加速原生库与 1.21.11 的 `BufferBuilder` 不兼容（`NoSuchFieldError: class_287.buffer`），
  现在会自动回退到 Java 渲染路径，不再崩客户端。
- TaCZ 联动：`rip/ysm/compat/gun/tacz/fabric/` 下补全了动画状态机、molang 变量与事件实现
  （官方 Fabric 构建里这块是空壳，见下）。

## 和另一个仓库的关系

`ysm-tacz-bridge`（[Haikimipingpong/ysm-tacz-bridge](https://github.com/Haikimipingpong/ysm-tacz-bridge)）
是一个独立的小桥接模组，用 mixin 给**官方 2.6.5.9 jar** 补上同样的 TaCZ 联动，不需要改官方 jar。
本仓库则是把 TaCZ 联动直接做进了移植版源码里。两者选一个用即可，不要同时装。

## 安装（玩家向）

1. Minecraft **1.21.11** + **Fabric Loader ≥ 0.19.0**（Java 21）。
2. 把 `openysm-*-fabric.jar` 丢进 `mods/`。
3. Fabric API 需要自己装（0.141.6+）。Architectury API 与 Cardinal Components 已经 jar-in-jar 打包，
   不用另装。
4. 可选：装 TaCZ（Timeless & Classics Zero）Refabricated 后，带 `tac:*` 动画的模型会自动切换
   持枪/瞄准/开火动作。

## 从源码构建

需要 JDK 21（Gradle 自身跑在 JDK 21+ 上），用 Gradle 9.x。

```bash
export GRADLE_USER_HOME=$PWD/.gradle-home
gradle build --console=plain
# 产物：build/libs/openysm-*-fabric.jar
```

`libs/` 里只需要放一个仅编译期使用的 jar（仓库不附带，也不打进产物）：

| 依赖 | 用途 |
|---|---|
| `TACZ-Refabricated-1.21.11.jar` | `modCompileOnly`，运行时可选；不装也能编译 |

## 已知限制

- macOS 上拿不到原生 SIMD 加速，模型渲染走 Java 逐顶点路径，大模型 / 多人场景 CPU 开销较高。
- 1.21.11 的 3D 预览（GUI 里的纸娃娃、模型卡）与原版有差异：纸娃娃「身体固定、头跟随」
  在这个移植版里很难做到——官方实现依赖 `PreviewEntityRegistry` + render state 朝向，
  这里朝向来自实体，且动画在 worker 线程计算。
- 权限系统是降级实现（只判断「是不是本机玩家」），多人服务器的管理命令不可靠。
- 没有测试用例；只在 macOS + Fabric 0.19.5 上实机跑过。
- 这是 1.21.11 的适配版，**不要**拿去连 1.20.1 的服务器，也不要和官方 Forge 版混装。

## 许可

- **上游 OpenYSM**：MIT，版权归 OpenYSM（[LICENSE.txt](LICENSE.txt) 原样保留）。
- **1.21.11 / Fabric 移植**：MIT，原作者不明（如认领即补署名，见上）。
- **本仓库的整理、修复与文档**：MIT，© 2026 Haikimipingpong。
- **模型资源**：默认模型 CC0；酒狐（Wine Fox）模型 CC BY-NC-SA 4.0，商用请自行确认。
- 本仓库**不含** Minecraft 本体代码或资源，也不含任何第三方 mod 的 jar。

## English

A community port of [OpenYSM](https://github.com/OpenYSM/OpenYSM) to **Minecraft 1.21.11 / Fabric**.
Unofficial; not affiliated with or endorsed by the OpenYSM authors.

**Provenance, in short:** upstream OpenYSM ships **Minecraft 1.20.1 / Forge only** — there is no
1.21.11 or Fabric release by its authors. The 1.21.11 / Fabric code in this repository was **found
inside a third-party modpack** as a prebuilt `openysm-fabric-2.6.5.9-mc1.21.11.jar` with no author,
repository or contact information attached. **The original porter of that adaptation is unknown**,
and the copyright for the 1.21.11/Fabric port belongs to them. This repository only turns it into
readable, rebuildable source, fixes the problems hit in practice, and documents where it came from.
If you are that porter, please open an issue so we can credit you — or ask us to take it down.

Everything here is MIT-licensed, matching upstream. No Mojang assets and no third-party mod jars
are included.

**Companion repository:** [YSM x TaCZ Bridge](https://github.com/Haikimipingpong/ysm-tacz-bridge) —
a mixin patch that adds TaCZ support to the *official* 2.6.5.9 jar. TaCZ support is already built
into this port, so install one or the other, never both.
