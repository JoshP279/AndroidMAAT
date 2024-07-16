function path_fill(ctx, gd, para)
{
	ctx.fillStyle = para.clr;
	ctx.beginPath();
	var glen = gd.length;
	var i = 0;
	while(i < glen)
	{
		switch(gd[i])
		{
		case 0:
        	ctx.moveTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy);
        	i += 3;
			break;
		case 1:
            ctx.lineTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy);
        	i += 3;
			break;
		case 2:
			ctx.quadraticCurveTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy,
				gd[i+3] * para.xscale + para.orgx, gd[i+4] * para.yscale + para.orgy);
			i += 5;
			break;
		case 3:
			ctx.bezierCurveTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy,
				gd[i+3] * para.xscale + para.orgx, gd[i+4] * para.yscale + para.orgy,
				gd[i+5] * para.xscale + para.orgx, gd[i+6] * para.yscale + para.orgy);
			i += 7;
			break;
		default:
			i++;
			ctx.closePath();
			break;
		}
	}
	if(para.evenodd)
		ctx.fill('evenodd');
	else
		ctx.fill();
}

function path_stroke(ctx, gd, para)
{
	ctx.strokeStyle = para.clr;
	ctx.beginPath();
	switch(para.style >> 8)//line join
	{
	case 1:
		ctx.lineJoin = "round";
		break;
	case 2:
		ctx.lineJoin = "bevel";
		break;
	default:
		ctx.lineJoin = "miter";
		break;
	}
	switch(para.style & 255)//line cap
	{
	case 1:
		ctx.lineCap = "round";
		break;
	case 2:
		ctx.lineCap = "square";
		break;
	default:
		ctx.lineCap = "butt";
		break;
	}
	ctx.lineWidth = para.width;
	var glen = gd.length;
	var i = 0;
	while(i < glen)
	{
		switch(gd[i])
		{
		case 0:
        	ctx.moveTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy);
        	i += 3;
			break;
		case 1:
            ctx.lineTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy);
        	i += 3;
			break;
		case 2:
			ctx.quadraticCurveTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy,
				gd[i+3] * para.xscale + para.orgx, gd[i+4] * para.yscale + para.orgy);
			i += 5;
			break;
		case 3:
			ctx.bezierCurveTo(gd[i+1] * para.xscale + para.orgx, gd[i+2] * para.yscale + para.orgy,
				gd[i+3] * para.xscale + para.orgx, gd[i+4] * para.yscale + para.orgy,
				gd[i+5] * para.xscale + para.orgx, gd[i+6] * para.yscale + para.orgy);
			i += 7;
			break;
		default:
			i++;
			ctx.closePath();
			break;
		}
	}
	ctx.stroke();
}

function tpath_fill(ctx, txt, sval)
{
	var tclr = txt[1];
	var tw = sval*txt[2]*0.01;
	var th = sval*txt[3]*0.01;
	var txts = txt[4];
	var ic = 0;
	for(ic = 0; ic < txts.length; ic++)
	{
		var tnode = txts[ic];
		path_fill(ctx, tnode[2],{clr:tclr,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval,evenodd:false});
	}
}

function tpath_stroke(ctx, txt, sval)
{
	var tclr = txt[1];
	var tw = sval*txt[2]*0.01;
	var th = sval*txt[3]*0.01;
	var lw = sval*txt[4];
	var ls = txt[5];
	var txts = txt[6];
	var ic = 0;
	for(ic = 0; ic < txts.length; ic++)
	{
		var tnode = txts[ic];
		path_stroke(ctx, tnode[2],{clr:tclr,width:lw,style:ls,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval});
	}
}

function tpath_stroke_fill(ctx, txt, sval)
{
	var tclr = txt[1];
	var lclr = txt[2];
	var tw = sval*txt[3]*0.01;
	var th = sval*txt[4]*0.01;
	var lw = sval*txt[5];
	var ls = txt[6];
	var txts = txt[7];
	var ic = 0;
	for(ic = 0; ic < txts.length; ic++)
	{
		var tnode = txts[ic];
		path_fill(ctx, tnode[2],{clr:tclr,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval,evenodd:false});
		path_stroke(ctx, tnode[2],{clr:lclr,width:lw,style:ls,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval});
	}
}

function draw_pg(ctx, def, cons, zoom, pix)
{
    var sval = zoom*pix;
    var ic =0;
    for(ic = 0; ic < cons.length; ic++)
    {
    	var con = cons[ic];
    	switch(con[0])
    	{
		case 0://text fill, [type,x,y,c,fw,fh,g]
			//con.x : orgx
			//con.y : orgy
			//con.c : fill color
			//con.fw : font size
			//con.fh : font size
			//con.g : glyph data
		    //path_fill(ctx, con[6], {clr:con[3],xscale:sval*con[4]*0.01,yscale:sval*con[5]*0.01,orgx:con[1]*sval,orgy:con[2]*sval,evenodd:false});
			tpath_fill(ctx, con, sval);
			break;
		case 1://text stroke, [type,x,y,c,fw,fh,w,s,g]
			//con.x : orgx
			//con.y : orgy
			//con.c : stroke color
			//con.fw : font size
			//con.fh : font size
			//con.w : line width
			//con.s : line style((join<<8)|cap)
			//con.g : glyph data
		    //path_stroke(ctx, con[8], {clr:con[3],width:con[6],style:con[7],scale:sval*con[4]*0.01,yscale:sval*con[5]*0.01,orgx:con[1]*sval,orgy:con[2]*sval});
		    tpath_stroke(ctx, con, sval);
			break;
		case 2://text fill and stroke. [type,x,y,c,cs,fw,fh,w,s,g]
			//con.x : orgx
			//con.y : orgy
			//con.c : fill color
			//con.cs : stroke color
			//con.fw : font size
			//con.fh : font size
			//con.w : line width
			//con.s : line style((join<<8)|cap)
			//con.g : glyph data
		    //path_fill(ctx, con[9], {clr:con[3],scale:sval*con[5]*0.01,yscale:sval*con[6]*0.01,orgx:con[1]*sval,orgy:con[2]*sval,evenodd:false});
		    //path_stroke(ctx, con[9], {clr:con[4],width:con[7],style:con[8],scale:sval*con[5]*0.01,yscale:sval*con[6]*0.01,orgx:con[1]*sval,orgy:con[2]*sval});
		    tpath_stroke_fill(ctx, con, sval);
			break;
		case 3://vector fill. [type,c,s,g]
			//con.c : fill color
			//con.s : fill style
			//con.g : glyph data
	    	path_fill(ctx, con[3], {clr:con[1],xscale:sval,yscale:sval,orgx:0,orgy:0,evenodd:(con[2]==0)});
			break;
		case 10://draw image. [type,x,y,w,h,f,i]
			if(con[5] == 0)
				ctx.drawImage(con[7], con[1]*sval, con[2]*sval, con[3]*sval, con[4]*sval);
			else
			{
				ctx.scale(1, -1);
				var ch = def.h * sval;
				ctx.drawImage(con[7], con[1]*sval, -(con[2] + con[4])*sval, con[3]*sval, con[4]*sval);
				ctx.scale(1, -1);
			}
			con[7] = null;
			break;
    	}
    }
}

function get_pageno(pid)
{
	var pcnt = pg_defs.length;
	var pcur = 0;
	while(pcur < pcnt)
	{
		if(pg_defs[pcur].n == pid) return pcur;
		pcur++;
	}
	return -1;
}

function draw_page(pgno, cid, pid, zoom, pix)
{
    var canvas = document.getElementById(cid);
    var def = pg_defs[pgno];
    var sval = zoom*pix;
    canvas.width = def.w * sval;
    canvas.height = def.h * sval;
    canvas.style.width = def.w * zoom + "px";
    canvas.style.height = def.h * zoom + "px";
    var ctx = canvas.getContext("2d");
    var cons = pg_cons[pgno];
    var img_cnt = 0;
    var con_cnt = cons.length;
    for(i = 0; i < con_cnt; i++)
    {
    	var con = cons[i];
    	if(con[0] == 10)
    	{
    		img_cnt++;
    		con[7] = new Image();
			con[7].src = con[6];
    	}	
    }
    if(img_cnt == 0) draw_pg(ctx, def, cons, zoom, pix);
    else
   	{
	    for(i = 0; i < con_cnt; i++)
	    {
	    	var con = cons[i];
	    	if(con[0] == 10)
	    	{
				con[7].onload = function(){
					img_cnt--;
					if(img_cnt == 0)
						draw_pg(ctx, def, cons, zoom, pix);
				};
	    	}	
	    }
	}
    
    var panel = document.getElementById(pid);
    panel.style.width = def.w * sval;
    panel.style.height = def.h * sval;
    var txts = pg_txts[pgno];
    var sele = "";
    for (i = 0; i < txts.length; i++)
    {
        var tn = txts[i];
        var nele = '<div class=\"rdtxt\" style=\"left:' + tn[0] * zoom + 'px;top:' + tn[1] * zoom + 'px;font-size:' + tn[2] * zoom + 'px;width:' + tn[4] * zoom + 'px;letter-spacing:' + tn[3] * zoom + 'px;\">' + tn[6] + '</div>';
        sele += nele;
    }
    var annots = pg_annots[pgno];
    for (i = 0; i < annots.length; i++)
    {
    	var an = annots[i];
   		var nele = '<div style=\"position:absolute;left:' + an[0] * zoom + 'px;top:' + an[1] * zoom + 'px;width:' + an[2] * zoom + 'px;height:' + an[3] * zoom + 'px;\"';
    	if(an[4] == 1)//uri
    	{
    		nele += "onclick=\"window.open('" + an[5] + "');\"></div>";
    	}
    	else if(an[4] == 3)//attachment
    	{
    		nele += "onclick=\"window.open('" + an[5] + "');\"></div>";
    	}
    	else
		{
			//var pageno = get_pageno(an[5]);
			//if(pageno < 0) alert("page not exists");
			//else nele += "onclick=\"gotoPage(" + pageno + ");\"></div>";
    		nele += "onclick=\"alert('page id:" + an[5] + "');\"></div>";
		}
        sele += nele;
    }
    panel.innerHTML = sele;
}
