package dev.faiths.module.player;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.util.ArrayList;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleInsult extends CheatModule {
    public ValueMode priority = new ValueMode("Mode", new String[] { "BWMIsland", "CSGO", "NSFW" }, "BWMIsland");
    private EntityPlayer target;
    private ArrayList<String> bwmIslandAbuseContent = new ArrayList<>();
    private ArrayList<String> csgoAbuseContent = new ArrayList<>();
    private ArrayList<String> nsfwAbuseContent = new ArrayList<>();

    public ModuleInsult() {
        super("Insult", Category.FUN);
        bwmIslandAbuseContent.add("@ %s 布吉岛电脑版开服时入账如同大风刮 现在我已经轻而易举随便开上了宝马");
        bwmIslandAbuseContent.add("@ %s 随随便便打倒嗨客庭吉吉岛一家独大 风格纳纹木糖醇来开桂也是随便拷打");
        bwmIslandAbuseContent.add("@ %s 摁着外桂打到布吉岛外桂认输叫爸爸 写外桂的地址野爹证也是随便的颁发");
        bwmIslandAbuseContent.add("@ %s 你们这些外桂去别的Server别来我家");
        bwmIslandAbuseContent.add("@ %s 你妖猫大跌的远控随便给你电脑仙人跳 后门被发现了我操");
        bwmIslandAbuseContent.add("@ %s 赶紧发篇文章致歉给外桂完爆 沈阳珠海滨州全都弱爆");
        bwmIslandAbuseContent.add("@ %s 我们野爹证给外桂 ban到仰天长啸");
        bwmIslandAbuseContent.add("@ %s 珍九鼎食万钱 我要开服圈钱");
        bwmIslandAbuseContent.add("@ %s 开个吉吉岛圈钱 把我钱包充填");
        bwmIslandAbuseContent.add("@ %s 圈光你们这群傻逼");
        bwmIslandAbuseContent.add("@ %s 我们修不好疾跑 我们圈钱圈到爆");
        bwmIslandAbuseContent.add("@ %s 我们要打击黑客 不然圈钱没着落");
        bwmIslandAbuseContent.add("@ %s 我们封禁机器码 吓死黑客的亲妈");
        bwmIslandAbuseContent.add("@ %s 我们缝合反作弊 四个反作弊才接地气");
        bwmIslandAbuseContent.add("@ %s 我们追着风格打 因为要圈钱开宝马");
        bwmIslandAbuseContent.add("@ %s 圈钱卖 VIP只要1000元 圈死你们这帮脑残");
        bwmIslandAbuseContent.add("@ %s 由于强制疾跑更新后存断疾跑的问题，如果您遇到了请再次按下W以恢复疾跑，或者手动按下疾跑键");
        bwmIslandAbuseContent.add("@ %s 敢写外挂就给你们全部抓起来");
        bwmIslandAbuseContent.add("@ %s 尊贵座驾白色宝马摸爬滚打不会跨 任凭岁月风吹雨打宝马车主很潇洒");
        bwmIslandAbuseContent.add("@ %s 宝马车主万水千山一路高歌再扬帆 纵享丝滑踏云端宝马车主不一般");
        bwmIslandAbuseContent.add("@ %s 名号响彻六扇门万人呐喊你男神 陪你望那岁月星辰坐下宝马可听闻");
        bwmIslandAbuseContent.add("@ %s 宝马车主天空海阔驾驶宝马多欢乐 今后生活朝阳不落人群之中最闪烁");
        bwmIslandAbuseContent.add("@ %s 宝马香车显富贵，妖猫圈钱手段妙。虚荣心起欲望生，陷入圈套难自拔。");
        bwmIslandAbuseContent.add("@ %s 开挂还开出优越感了，真是可笑。违法的事情都敢干。等着进去无期徒刑吧。");
        bwmIslandAbuseContent.add("@ %s 杂牌挂杂牌人用，大脑发育正常的人都不会开这个");
        bwmIslandAbuseContent.add("@ %s 你是不懂法律吗，外挂都被抓进去多少个了。我马上就提交给警方，等着进去无期徒刑吧。");
        bwmIslandAbuseContent.add("@ %s 666这个入是桂");
        csgoAbuseContent.add("@ %s Missed %s due to correction");
        csgoAbuseContent.add("@ %s Missed %s due to spread");
        csgoAbuseContent.add("@ %s Missed %s due to prediction error");
        csgoAbuseContent.add("@ %s Missed %s due to invalid backtrack");
        csgoAbuseContent.add("@ %s Missed %s due to ?");
        csgoAbuseContent.add("@ %s Shot at head, and missed head, but hit anyways because of spread (lol)");
        csgoAbuseContent.add("@ %s Missed %s due to resolver");
        nsfwAbuseContent.add("@ %s 被哥哥蒙在鼓里有什么意思呢，蒙在被子里才有趣");
        nsfwAbuseContent.add("@ %s 什么是b 我没有b 我的双腿间是哥哥的饮水机");
        nsfwAbuseContent.add("@ %s 哥哥我声音好听吗，你再进来一点还可以更好听");
        nsfwAbuseContent.add("@ %s 在你的紧逼之下 我终于缴械投降");
        nsfwAbuseContent.add("@ %s 姐姐不会做菜但是下面一流，弟弟你饿吗");
        nsfwAbuseContent.add("@ %s 有些事不用在一晚内做完的 我们又不赶时间 可以每晚都做一做");
        nsfwAbuseContent.add("@ %s 哥哥会扎针吗？医生说我需要扎一针37.2℃的蛋白质混合溶液");
        nsfwAbuseContent.add("@ %s 明明对哥哥的爱不掺水分，可是想哥哥的时候，为什么总是湿湿的");
        nsfwAbuseContent.add("@ %s 我夹娃娃不行，夹你还可以");
        nsfwAbuseContent.add("@ %s 听说电钻钻到最下面的时候有白色瀑布");
        nsfwAbuseContent.add("@ %s 我顶不顶的住没关系，你一直顶就好了");
        nsfwAbuseContent.add("@ %s 想用腿给哥哥量量腰围");
        nsfwAbuseContent.add("@ %s 我喜欢你脸红红的样子，更喜欢你气喘吁吁的样子");
        nsfwAbuseContent.add("@ %s 一想到你呀，我的身体就像进了回南天，心里和身上总是湿漉漉的");
        nsfwAbuseContent.add("@ %s 你像风来了又走，我的身体满了又空");
        nsfwAbuseContent.add("@ %s 是你俯卧撑，还是我深蹲");
        nsfwAbuseContent.add("@ %s 不知道我的糖是否能融进你38.5的海洋");
        nsfwAbuseContent.add("@ %s 长的吓人和长得吓人是两个意思");
        nsfwAbuseContent.add("@ %s 别人一上来就喜欢，你不喜欢我是因为你没上来");
        nsfwAbuseContent.add("@ %s 我顶不顶的住没关系，你一直顶就好了");
        nsfwAbuseContent.add("@ %s 每天只想跟你做四件事，一日三餐");
        nsfwAbuseContent.add("@ %s 我以为感情要用心，没想到你喜欢用力");
        nsfwAbuseContent.add("@ %s 草莓尖尖我吃我的尖尖你吃");
        nsfwAbuseContent.add("@ %s 吃过那么多的火腿肠，发现还是你给我的好吃");
        nsfwAbuseContent.add("@ %s 想和哥哥日出而做日落而息");
        nsfwAbuseContent.add("@ %s 我的两腿之间是地狱，你的两腿之间是恶魔，把恶魔关进地狱，我们就能一起上天堂");
        nsfwAbuseContent.add("@ %s 想和你搭一个只有我和你的水平和垂直象限");
        nsfwAbuseContent.add("@ %s 你说生活进退两难，那我就陪你进进出出");
        nsfwAbuseContent.add("@ %s 37摄氏度的牛奶很好喝只是吸管有点粗");
        nsfwAbuseContent.add("@ %s 什么是B？我没有B...我双腿之间是哥哥的饮水机");
    }

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (target != null && !mc.theWorld.loadedEntityList.contains(target)) {
            switch (priority.getValue()) {
                case "CSGO":
                    mc.thePlayer.sendChatMessage(csgoAbuseContent.get((int)(Math.random() * csgoAbuseContent.size())).replace("%s", target.getDisplayName().getUnformattedTextForChat()));
                    break;
                case "NSFW":
                    mc.thePlayer.sendChatMessage(nsfwAbuseContent.get((int)(Math.random() * nsfwAbuseContent.size())).replace("%s", target.getDisplayName().getUnformattedTextForChat()));
                    break;
                default:
                case "BWMIsland":
                    mc.thePlayer.sendChatMessage(bwmIslandAbuseContent.get((int)(Math.random() * bwmIslandAbuseContent.size())).replace("%s", target.getDisplayName().getUnformattedTextForChat()));
                    break;
            }
            target = null;
        }
    };

    private final Handler<PacketEvent> attackEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof C02PacketUseEntity)) return;
        C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
        if (packet.getEntityFromWorld(mc.theWorld) instanceof EntityPlayer) {
            target = (EntityPlayer) packet.getEntityFromWorld(mc.theWorld);
        }
    };

    private final Handler<WorldEvent> worldEventHandler = event -> {
        target = null;
    };
}
