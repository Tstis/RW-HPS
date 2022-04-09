function addTable(p,data,sty,witNo,call,colorSchema){
    if(witNo) {
        data.forEach((r,i)=>{
            if(i==0) r.unshift("序号")
            else r.unshift(i)
        })
    }
    let tab = document.createElement("table");
    tab.classList.add(sty)
    let tbd = document.createElement("tbody");
    tab.append(tbd)
    data.forEach((r,i)=>{
        let tr = document.createElement("tr");
        colorSchema(r,i,tr)
        r.forEach(t=>{
            let el = document.createElement(i==0?"th":"td");

            el.innerText=t;
            if(call) el.onclick=function (e){
                call(t);
            }
            tr.append(el);
        })
        tbd.append(tr);
    })
    p.append(tab);
}

function colorWalk(a,b){
    if(!a) a=0; if(!b) b=Math.pow(16,6)
    return "#"+Math.round(a+(b-a)*Math.random()).toString(16);
}
function colorWalkS(h,l,a,sa,sb){
    if(!sa) sa=0; if(!sb) sb=100;
    let s=randomRange(sa,sb)
    return "hsla("+h+","+s+"%,"+l+"%,"+a+")";
}
function colorWalkH(s,l,a,ha,hb){
    if(!ha) ha=0; if(!hb) hb=100;
    let h=randomRange(ha,hb)
    return "hsla("+h+","+s+"%,"+l+"%,"+a+")";
}
function colorWalkHA(s,l,aa,ab,ha,hb){
    if(!ha) ha=0; if(!hb) hb=100;
    if(!aa) aa=0; if(!ab) ab=1;
    let h=randomRange(ha,hb)
    let a=randomRange(aa,ab)
    return "hsla("+h+","+s+"%,"+l+"%,"+a+")";
}
function randomRange(a,b){
    return Math.random()*(b-a)+a;
}
function releaseBlock(){
    let bC = document.getElementById("block-cover");
    if(bC){
        bC.innerHTML='';
    }
}
function blockOther(obj){
    let w=document.documentElement.clientWidth;
    let h=document.documentElement.clientHeight;
    console.log(w)
    console.log(h)
    let a=obj.offsetTop-window.pageYOffset;
    let c=h-obj.clientHeight-a;
    if(a>h-40||c>h-40){
        let oPos=obj.offsetTop-h/2+obj.clientHeight/2
        document.documentElement.scrollTop = oPos;
        document.body.scrollTop = oPos;
        a=obj.offsetTop-window.pageYOffset;
        c=h-obj.clientHeight-a;
    }

    let d=obj.offsetLeft-window.pageXOffset;
    let b=w-obj.clientWidth-d;

    let bC = document.getElementById("block-cover");
    if(bC){
        bC.innerHTML='';
        bC.classList.remove("block-cover")
    }else{
        bC=document.createElement("div")
        bC.id="block-cover"
        bC.style.position="fixed";
        bC.style.top="0px"
        bC.style.left="0px"
        bC.style.zIndex="9999"
        document.body.append(bC)
    }
    if(d>0){
        let dL = document.createElement("div");
        dL.style.width=d+"px"
        dL.style.height=h+"px";
        dL.style.left="0px"
        bC.append(dL)
    }
    if(b>0){
        let bL = document.createElement("div");
        bL.style.width=b+"px"
        bL.style.height=h+"px";
        bL.style.right="0px"
        bC.append(bL)
    }
    if(a>0){
        let aL = document.createElement("div");
        let wi=w
        if(d>0){
            aL.style.left=d+"px"
            wi-=d;
        }else aL.style.left=0+"px"
        if(b>0) wi-=b;
        aL.style.width=wi+"px";
        aL.style.height=a+"px";
        bC.append(aL);
    }
    if(c>0){
        let cL = document.createElement("div");
        cL.style.bottom="0px"
        let wi=w
        if(d>0){
            cL.style.left=d+"px"
            wi-=d;
        }
        if(b>0) wi-=b;
        cL.style.width=wi+"px";
        cL.style.height=c+"px";
        bC.append(cL);
    }
    setTimeout(()=>bC.classList.add("block-cover"),100)
    bC.childNodes.forEach(x=>{
        x.style.position="fixed"
        x.onclick=releaseBlock;
    })
}
function styleAttach(){
    let sty=document.createElement("style");
    sty.innerHTML=styleSheet
    document.head.append(sty)
}
styleSheet=`
        table.lovTab {
            font-size:11px;
            color:#333333;
            border-width: 1px;
            border-color: #999999;
            border-collapse: collapse;
            box-shadow:1px 3px 10px 2px rgb(144 144 144 / 29%);
        }
        table.lovTab th {
            background-color: #acffe480;
            border-width: 1px;
            padding: 8px;
            border-style: solid;
            border-color: #999999;
        }
        table.lovTab th:hover{
            background-color: #42bddc80;
            transition: 1s linear;
        }
        table.lovTab td {
            background-color: #edeee280;
            border-width: 1px;
            padding: 8px;
            border-style: solid;
            border-color: #999999;
        }
        table.lovTab td:hover {
            background-color: #ebf39d80;
            transition: 1s linear;
        }
`