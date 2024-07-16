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

function path_load(istr)
{
	var pns_cnt = istr.read_i32();
	var pns = new Array(pns_cnt);
	var idx = 0;
	while(idx < pns_cnt)
	{
		var ntype = istr.read_u8();
		pns[idx++] = ntype;
		switch(ntype)
		{
		case 2:
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			break;
		case 3:
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			break;
		case 4:
			break;
		default:
			pns[idx++] = istr.read_i32() * 0.01;
			pns[idx++] = istr.read_i32() * 0.01;
			break;
		}
	}
	return pns;
}

function tpath_fill(istr, ctx, page, txt, sval)
{
	var tclr = txt[1];
	var tw = sval*txt[2]*0.01;
	var th = sval*txt[3]*0.01;
	var txts = txt[4];
	var ic = 0;
	for(ic = 0; ic < txts.length; ic++)
	{
		var tnode = txts[ic];
		var key = "c" + tnode[2];
		var pns = page.cache[key];
		if(!pns)
		{
			istr.seek(tnode[2]);//offset to glyph def
			pns = path_load(istr);
			page.cache[key] = pns;
		}
		path_fill(ctx, pns,{clr:tclr,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval,evenodd:false});
	}
}

function tpath_stroke(istr, ctx, page, txt, sval)
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
		var key = "c" + tnode[2];
		var pns = page.cache[key];
		if(!pns)
		{
			istr.seek(tnode[2]);//offset to glyph def
			pns = path_load(istr);
			page.cache[key] = pns;
		}
		path_stroke(ctx, pns,{clr:tclr,width:lw,style:ls,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval});
	}
}

function tpath_stroke_fill(istr, ctx, page, txt, sval)
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
		var key = "c" + tnode[2];
		var pns = page.cache[key];
		if(!pns)
		{
			istr.seek(tnode[2]);//offset to glyph def
			pns = path_load(istr);
			page.cache[key] = pns;
		}
		path_fill(ctx, pns,{clr:tclr,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval,evenodd:false});
		path_stroke(ctx, pns,{clr:lclr,width:lw,style:ls,xscale:tw,yscale:th,orgx:tnode[0]*sval,orgy:tnode[1]*sval});
	}
}

function draw_pg(istr, ctx, page, sval)
{
    var ic =0;
    for(ic = 0; ic < page.cons.length; ic++)
    {
    	var con = page.cons[ic];
    	switch(con[0])
    	{
		case 0://text fill, [type,x,y,c,fw,fh,g]
			tpath_fill(istr, ctx, page, con, sval);
			break;
		case 1://text stroke, [type,x,y,c,fw,fh,w,s,g]
		    tpath_stroke(istr, ctx, page, con, sval);
			break;
		case 2://text fill and stroke. [type,x,y,c,cs,fw,fh,w,s,g]
		    tpath_stroke_fill(istr, ctx, page, con, sval);
			break;
		case 3://vector fill. [type,c,s,g]
	    	path_fill(ctx, con[3], {clr:con[1],xscale:sval,yscale:sval,orgx:0,orgy:0,evenodd:(con[2]==0)});
			break;
		case 10://draw image. [type,x,y,w,h,f,i]
			if(con[5] == 0)
				ctx.drawImage(con[7], con[1]*sval, con[2]*sval, con[3]*sval, con[4]*sval);
			else
			{
				ctx.scale(1, -1);
				var ch = page.h * sval;
				ctx.drawImage(con[7], con[1]*sval, -(con[2] + con[4])*sval, con[3]*sval, con[4]*sval);
				ctx.scale(1, -1);
			}
			con[7] = null;
			break;
    	}
    }
}

var refs_cnt = 0;
function load_doc(istr)
{
	istr.seek(istr.m_len - 4);
	refs_cnt = istr.read_i32();
}

function load_tfill(istr)
{
	var fnode = new Array(5);
	var b = istr.read_u8();
	var g = istr.read_u8();
	var r = istr.read_u8();
	var a = istr.read_u8();
	if(a > 253) fnode[1] = "rgb(" + r + "," + g + "," + b + ")";
	else fnode[1] = "rgba(" + r + "," + g + "," + b + "," + a/255.0 + ")";
	fnode[2] = istr.read_i32() * 0.01;//w
	fnode[3] = istr.read_i32() * 0.01;//h
	var ccnt = istr.read_u16();
	var ic = 0;
	var chars = new Array(ccnt);
	for(ic = 0; ic < ccnt; ic++)
	{
		var cdat = new Array(3);
		cdat[0] = istr.read_i32() * 0.01;
		cdat[1] = istr.read_i32() * 0.01;
		cdat[2] = istr.read_i32();
		chars[ic] = cdat;
	}
	fnode[4] = chars;
	return fnode;
}

function load_tstroke(istr)
{
	var fnode = new Array(7);
	var b = istr.read_u8();
	var g = istr.read_u8();
	var r = istr.read_u8();
	var a = istr.read_u8();
	if(a > 253) fnode[1] = "rgb(" + r + "," + g + "," + b + ")";
	else fnode[1] = "rgba(" + r + "," + g + "," + b + "," + a/255.0 + ")";
	fnode[2] = istr.read_i32() * 0.01;//w
	fnode[3] = istr.read_i32() * 0.01;//h
	fnode[4] = istr.read_i32() * 0.01;//line_w
	fnode[5] = istr.read_u16();//line style
	var ccnt = istr.read_u16();
	var ic = 0;
	var chars = new Array(ccnt);
	for(ic = 0; ic < ccnt; ic++)
	{
		var cdat = new Array(3);
		cdat[0] = istr.read_i32() * 0.01;
		cdat[1] = istr.read_i32() * 0.01;
		cdat[2] = istr.read_i32();
		chars[ic] = cdat;
	}
	fnode[6] = chars;
	return fnode;
}

function load_tstroke_fill(istr)
{
	var fnode = new Array(8);
	var b = istr.read_u8();
	var g = istr.read_u8();
	var r = istr.read_u8();
	var a = istr.read_u8();
	if(a > 253) fnode[1] = "rgb(" + r + "," + g + "," + b + ")";
	else fnode[1] = "rgba(" + r + "," + g + "," + b + "," + a/255.0 + ")";
	b = istr.read_u8();
	g = istr.read_u8();
	r = istr.read_u8();
	a = istr.read_u8();
	if(a > 253) fnode[2] = "rgb(" + r + "," + g + "," + b + ")";
	else fnode[2] = "rgba(" + r + "," + g + "," + b + "," + a/255.0 + ")";
	fnode[3] = istr.read_i32() * 0.01;//w
	fnode[4] = istr.read_i32() * 0.01;//h
	fnode[5] = istr.read_i32() * 0.01;//line_w
	fnode[6] = istr.read_u16();//line style
	var ccnt = istr.read_u16();
	var ic = 0;
	var chars = new Array(ccnt);
	for(ic = 0; ic < ccnt; ic++)
	{
		var cdat = new Array(3);
		cdat[0] = istr.read_i32() * 0.01;
		cdat[1] = istr.read_i32() * 0.01;
		cdat[2] = istr.read_i32();
		chars[ic] = cdat;
	}
	fnode[7] = chars;
	return fnode;
}

function load_path_fill(istr)
{
	var fnode = new Array(4);
	var b = istr.read_u8();
	var g = istr.read_u8();
	var r = istr.read_u8();
	var a = istr.read_u8();
	if(a > 253) fnode[1] = "rgb(" + r + "," + g + "," + b + ")";
	else fnode[1] = "rgba(" + r + "," + g + "," + b + "," + a/255.0 + ")";
	fnode[2] = istr.read_u8();//winding?
	fnode[3] = path_load(istr);
	return fnode;
}

function load_image(istr)
{
	var fnode = new Array(6);
	fnode[1] = istr.read_i32() * 0.01;//x
	fnode[2] = istr.read_i32() * 0.01;//y
	fnode[3] = istr.read_i32() * 0.01;//w
	fnode[4] = istr.read_i32() * 0.01;//h
	fnode[5] = istr.read_u8();//flag
	fnode[6] = istr.read_utf8();
	return fnode;
}

function load_con(istr)
{
	var ctype = istr.read_u8();
	var con = null;
	var tchars_cnt = 0;
	var tchars;
	switch(ctype)
	{
	case 0:
		con = load_tfill(istr);
		break;
	case 1:
		con = load_tstroke(istr);
		break;
	case 2:
		con = load_tstroke_fill(istr);
		break;
	case 3:
		con = load_path_fill(istr);
		break;
	case 10:
		con = load_image(istr);
		break;
	}
	if(con != null) con[0] = ctype;
	return con;
}

function load_page(istr, pgno)
{
	istr.seek(istr.m_len - (refs_cnt + 1) * 4 + pgno * 4);
	var poff = istr.read_i32();
	istr.seek(poff);
	var page =
	{
		w:0,
		h:0,
		n:0,
		cons:null,
		txts:null,
		annots:null,
		cache:{}
	}
	page.w = istr.read_i32() * 0.01;
	page.h = istr.read_i32() * 0.01;
	page.n = istr.read_i32();
	var cons_cnt = istr.read_i32();
	var cons = new Array(cons_cnt);
	var ic = 0;
	for(ic = 0; ic < cons_cnt; ic++)
	{
		cons[ic] = load_con(istr);
	}
	page.cons = cons;

	var txts_cnt = istr.read_i32();
	var txts = new Array(txts_cnt);
	var it = 0;
	for(it = 0; it < txts_cnt; it++)
	{
		var txt = new Array(7);
		txt[0] = istr.read_i32() * 0.01;
		txt[1] = istr.read_i32() * 0.01;
		txt[2] = istr.read_i32() * 0.01;
		txt[3] = istr.read_i32() * 0.01;
		txt[4] = istr.read_i32() * 0.01;
		txt[5] = istr.read_u8();
		txt[6] = istr.read_utf16();
		txts[it] = txt;
	}
	page.txts = txts;

	var annots_cnt = istr.read_i32();
	var annots = new Array(annots_cnt);
	var ia = 0;
	for(ia = 0; ia < annots_cnt; ia++)
	{
		var annot = new Array(6);
		annot[0] = istr.read_i32() * 0.01;
		annot[1] = istr.read_i32() * 0.01;
		annot[2] = istr.read_i32() * 0.01;
		annot[3] = istr.read_i32() * 0.01;
		annot[4] = istr.read_u8();
		if(annot[4] == 1)//uri
			annot[5] = istr.read_utf8();
		else if(annot[4] == 3)//uri
			annot[5] = istr.read_utf8();
		else if(annot[4] == 2)//goto
			annot[5] = istr.read_i32();
		else
			annot[5] = istr.read_i32();
		annots[ia] = annot;
	}
	page.annots = annots;
	return page;
}

function get_pageno(istr, pid)
{
	var pcnt = refs_cnt;
	var pcur = 0;
	while(pcur < pcnt)
	{
		istr.seek(istr.m_len - (refs_cnt + 1) * 4 + pgno * 4);
		var poff = istr.read_i32();
		istr.seek(poff + 8);
		poff = istr.read_i32();
		if(poff == pid) return pcur;
		pcur++;
	}
	return -1;
}

function draw_page(istr, pgno, cid, pid, zoom, pix)
{
	var page = load_page(istr, pgno);
    var canvas = document.getElementById(cid);
    var sval = zoom*pix;
    canvas.width = page.w * sval;
    canvas.height = page.h * sval;
    canvas.style.width = page.w * zoom + "px";
    canvas.style.height = page.h * zoom + "px";
    var ctx = canvas.getContext("2d");
    var cons = page.cons;
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
    if(img_cnt == 0) draw_pg(istr, ctx, page, sval);
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
						draw_pg(istr, ctx, page, sval);
				};
	    	}	
	    }
	}
    
    var panel = document.getElementById(pid);
    panel.style.width = page.w * sval;
    panel.style.height = page.h * sval;

    var txts = page.txts;
    var sele = "";
    for (i = 0; i < txts.length; i++)
    {
        var tn = txts[i];
        var nele = '<div class=\"rdtxt\" style=\"left:' + tn[0] * zoom + 'px;top:' + tn[1] * zoom + 'px;font-size:' + tn[2] * zoom + 'px;width:' + tn[4] * zoom + 'px;letter-spacing:' + tn[3] * zoom + 'px;\">' + tn[6] + '</div>';
        sele += nele;
    }
    var annots = page.annots;
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
			//var pageno = get_pageno(istr, an[5]);
			//if(pageno < 0) alert("page not exists");
			//else nele += "onclick=\"gotoPage(" + pageno + ");\"></div>";
    		nele += "onclick=\"alert('page id:" + an[5] + "');\"></div>";
		}
        sele += nele;
    }
    panel.innerHTML = sele;
}
