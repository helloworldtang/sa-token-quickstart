#!/usr/bin/env python3
"""生成 Sa-Token 统一权限中心架构图"""
from PIL import Image, ImageDraw, ImageFont
import os, math

W, H = 1200, 700
img = Image.new("RGB", (W, H), "#0d1117")
draw = ImageDraw.Draw(img)

def get_font(size):
    candidates = [
        "/System/Library/Fonts/PingFang.ttc",
        "/System/Library/Fonts/STHeiti Light.ttc",
        "/Library/Fonts/Arial.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
    ]
    for f in candidates:
        if os.path.exists(f):
            try:
                return ImageFont.truetype(f, size)
            except Exception:
                pass
    return ImageFont.load_default()

font_title = get_font(22)
font_bold  = get_font(15)
font_sm    = get_font(12)
font_tiny  = get_font(10)

C_BG="#0d1117"; C_CARD="#161b22"; C_BORDER="#30363d"
C_BLUE="#58a6ff"; C_GREEN="#3fb950"; C_RED="#f78166"; C_PURPLE="#d2a8ff"
C_TEXT="#e6edf3"; C_SUB="#8b949e"
C_GW="#1f6feb"; C_SVC="#238636"; C_RDS="#da3633"; C_AUTHC="#6e40c9"

def rr(draw, xy, r, **kw):
    draw.rounded_rectangle(xy, radius=r, **kw)

def box(dx, dy, dw, dh, title, lines, **kw):
    bg=kw.get("bg",C_CARD); bd=kw.get("border",C_BORDER); acc=kw.get("acc",C_BLUE)
    tag=kw.get("tag",""); f_lbl=kw.get("f_lbl",font_bold); f_sub=kw.get("f_sub",font_tiny)
    rr(draw,(dx,dy,dx+dw,dy+dh),8,fill=bg,outline=bd,width=1)
    ty=dy+12
    if tag:
        draw.rounded_rectangle((dx+8,dy+8,dx+8+len(tag)*7,dy+24),4,fill=acc)
        draw.text((dx+12,dy+9),tag,font=font_tiny,fill="#fff")
        ty=dy+30
    draw.text((dx+dw//2,ty),title,font=f_lbl,fill=C_TEXT,anchor="mm")
    if lines:
        for i,ln in enumerate(lines):
            draw.text((dx+dw//2,ty+18+i*14),ln,font=f_sub,fill=C_SUB,anchor="mm")

def arrow(dx1,dy1,dx2,dy2,col=C_BLUE,dash=False):
    if dash:
        length=math.sqrt((dx2-dx1)**2+(dy2-dy1)**2)
        if length==0: return
        dl=6; gl=4; cnt=int(length//(dl+gl))
        for i in range(cnt):
            t0=i*(dl+gl)/length; t1=min((i*(dl+gl)+dl)/length,1)
            draw.line([dx1+(dx2-dx1)*t0,dy1+(dy2-dy1)*t0,
                       dx1+(dx2-dx1)*t1,dy1+(dy2-dy1)*t1],fill=col,width=2)
    else:
        draw.line([dx1,dy1,dx2,dy2],fill=col,width=2)
    length=math.sqrt((dx2-dx1)**2+(dy2-dy1)**2)
    if length==0: return
    ux,uy=(dx2-dx1)/length,(dy2-dy1)/length
    ax,ay=dx2-ux*10,dy2-uy*10
    draw.polygon([(dx2,dy2),(ax+uy*5,ay-ux*5),(ax-uy*5,ay+ux*5)],fill=col)

def mid(dx1,dy1,dx2,dy2):
    return (dx1+dx2)//2,(dy1+dy2)//2

# ── 标题 ──
draw.text((W//2,40),"Sa-Token 全链路统一权限架构",font=font_title,fill=C_TEXT,anchor="mm")
draw.text((W//2,68),"Spring Cloud Gateway + Redis 分布式 Session + 微服务鉴权",font=font_sm,fill=C_SUB,anchor="mm")

# ── 客户端 ──
CW=160;CH=60;CX=W//2-CW//2;CY=100
box(CX,CY,CW,CH,"🌐 客户端",[">>> satoken=xxx-xxx-xxx"],
    bg="#1a2332",border=C_BLUE,acc=C_BLUE)

# 客户端 → 网关
arrow(CX+CW//2,CY+CH,CX+CW//2,220)
draw.text((CX+CW//2+8,CY+CH+28),"① 携带 Token 请求",font=font_tiny,fill=C_SUB)

# ── 网关 ──
GW_W=280;GW_H=130;GW_X=W//2-GW_W//2;GW_Y=210
box(GW_X,GW_Y,GW_W,GW_H,
    "☁️  Spring Cloud Gateway  (8080)",
    ["Sa-Token Reactor Filter","Token 读取 → Redis 验证","未登录→401 | 有效→放行"],
    bg="#0d1b2e",border=C_GW,acc=C_GW,f_lbl=font_bold,tag="GATEWAY")

# ── 业务服务 ──
SV_W=195;SV_H=115;SV_Y=405

# User Service
USR_X=40
box(USR_X,SV_Y,SV_W,SV_H,
    "👤  User Service  (8081)",
    ["StpInterfaceImpl","登录 / 获取用户信息","@SaCheckLogin/Permission"],
    bg="#0d2318",border=C_SVC,acc=C_SVC,tag="8081")

# Auth Service
AU_X=W//2-SV_W//2
box(AU_X,SV_Y,SV_W,SV_H,
    "🔐  Auth Service  (8083)",
    ["统一鉴权接口","StpUtil.login()","Token 校验 / API Key"],
    bg="#1a0d2e",border=C_AUTHC,acc=C_AUTHC,tag="8083")

# Order Service
ORD_X=W-SV_W-40
box(ORD_X,SV_Y,SV_W,SV_H,
    "📦  Order Service  (8082)",
    ["业务逻辑","@SaCheckPermission","\"order:create\""],
    bg="#0d2318",border=C_SVC,acc=C_SVC,tag="8082")

# 网关 → 各服务
for sx in [USR_X+SV_W//2, AU_X+SV_W//2, ORD_X+SV_W//2]:
    col = C_SVC if sx != AU_X+SV_W//2 else C_AUTHC
    arrow(GW_X+GW_W//2,GW_Y+GW_H,sx,SV_Y,col)
    names=["用户","鉴权","订单"]
    idx=["用户","鉴权","订单"].index(names[([USR_X+SV_W//2,AU_X+SV_W//2,ORD_X+SV_W//2].index(sx))])
    draw.text((GW_X+GW_W//2+8,GW_Y+GW_H+12),f"路由→{names[idx]}服务",font=font_tiny,fill=C_SUB)

# ── Redis ──
RW=300;RH=80;RX=W//2-RW//2;RY=565
box(RX,RY,RW,RH,
    "🟥  Redis  (6379)",
    ["satoken:login:session:10001  →  {userId, role, permissions}","Token 存储 | 分布式 Session | 限流计数"],
    bg="#2d0d0d",border=C_RDS,acc=C_RDS,f_lbl=font_bold,tag="STORAGE")

# 服务 → Redis
for sx in [USR_X+SV_W//2,AU_X+SV_W//2,ORD_X+SV_W//2]:
    arrow(sx,SV_Y+SV_H,RX+RW//2,RY,dash=True,col="#8b949e")
draw.text((RX+RW//2+8,SV_Y+SV_H+8),"Session 存储/读取",font=font_tiny,fill=C_SUB)

# ── 右侧技术栈 ──
VX=W-210
rr(draw,(VX,GW_Y,W-30,GW_Y+GW_H+SV_H+18),8,fill=C_CARD,outline=C_BORDER,width=1)
draw.text((VX+12,GW_Y+12),"🛠 技术栈",font=font_bold,fill=C_TEXT)
stack=[("Spring Boot","3.3.0"),("Spring Cloud","2023.0.3"),("Sa-Token","1.45.0"),
       ("Redis","最新"),("JDK","17+"),("MyBatis Plus","3.5.5")]
for i,(k,v) in enumerate(stack):
    y=GW_Y+38+i*22
    draw.text((VX+14,y),k,font=font_sm,fill=C_SUB)
    draw.text((VX+130,y),v,font=font_sm,fill=C_GREEN)

# ── 底部流程 ──
BY=RY+RH+12
draw.text((40,BY),"📌 认证流程：",font=font_sm,fill=C_TEXT)
steps=["① 客户端携带 satoken header 发起请求","② Gateway SaReactorFilter 读取 Token，查询 Redis 验证有效性",
       "③ Token 有效→路由到下游服务；无效→返回 401","④ 业务服务通过 @SaCheckPermission 注解进行权限校验"]
for i,s in enumerate(steps):
    draw.text((40,BY+22+i*16),s,font=font_tiny,fill=C_SUB)

# ── 图例 ──
LY=BY+90
draw.text((40,LY),"图例：",font=font_sm,fill=C_TEXT)
items=[(C_GW,"API 网关"),(C_SVC,"业务服务"),(C_AUTHC,"鉴权服务"),(C_RDS,"存储层"),(C_BLUE,"数据流向")]
for i,(c,lbl) in enumerate(items):
    lx=90+i*120
    rr(draw,(lx,LY-3,lx+14,LY+11),3,fill=c)
    draw.text((lx+18,LY+1),lbl,font=font_tiny,fill=C_SUB)

# ── 保存 ──
out="/Users/tangcheng/workspace/github/sa-token-quickstart/docs/images/sa-token-gateway-auth.png"
os.makedirs(os.path.dirname(out),exist_ok=True)
img.save(out,"PNG")
print(f"✅ 架构图已生成: {out}")
print(f"   尺寸: {W}x{H}")
