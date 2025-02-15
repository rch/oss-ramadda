/**
 * Copyright (c) 2008-2023 Geode Systems LLC
 */


var RamaddaUtils;
var RamaddaUtil;
var Ramadda = RamaddaUtils = RamaddaUtil  = {
    contents:{},
    currentRamaddaBase:null,

    currentSelector:null,
    initToggleTable:function(container) {
	$(container).find('.entry-arrow').click(function() {
	    let url = $(this).attr('data-url');
	    if(!url) return;
	    let title = $(this).attr('data-title')??'';
	    let handler = request => {
		if (request.responseXML == null) return;
		let xmlDoc = request.responseXML.documentElement;
		let script;
		let html;
		for (i = 0; i < xmlDoc.childNodes.length; i++) {
		    let childNode = xmlDoc.childNodes[i];
		    if (childNode.tagName == "javascript") {
			script = getChildText(childNode);
		    } else if (childNode.tagName == "content") {
			html = getChildText(childNode);
		    } else {}
		}
		if (!html) {
		    html = getChildText(xmlDoc);
		}
		if (html) {
		    html = HU.div(['style',HU.css('margin','5px','max-height','300px','overflow-y','auto')], html);
		    let dialog =  HU.makeDialog({content:html,my:"left top",at:"left bottom",
						 title:title,anchor:this,draggable:true,header:true,inPlace:false,stick:true});

		    Utils.checkTabs(html);
		}
		if (script) {
		    eval(script);
		}
	    }
            GuiUtils.loadXML(url, handler);
	});
    },
    selectInitialClick:function(event, selectorId, elementId, allEntries, selecttype, localeId, entryType,baseUrl,props) {
	if(RamaddaUtils.currentSelector) {
	    RamaddaUtils.currentSelector.cancel();
	}
	RamaddaUtils.currentSelector = RamaddaUtils.selectCreate(event, selectorId, elementId, allEntries, selecttype, localeId, entryType,baseUrl,props);
	return false;
    },
    selectCreate:function(event, selectorId, elementId, allEntries, selecttype, localeId, entryType, baseUrl,props) {
	let key = selectorId + (baseUrl||"");
	if (true || !selectors[key]) {
            return selectors[selectorId] = selectors[key] = new Selector(event, selectorId, elementId, allEntries, selecttype, localeId, entryType,baseUrl,props);
	} else {
            return selectors[key].handleClick(event);
	}
    },


    clearSelect:function(id) {
	selector = selectors[id];
	if (selector) {
            selector.clearInput();
	} else {
            //In case the user never clicked select
            let textComp = GuiUtils.getDomObject(id);
            let hiddenComp = GuiUtils.getDomObject(id + "_hidden");
            if (hiddenComp) {
		hiddenComp.obj.value = ""
            }
            if (textComp) {
		textComp.obj.value = ""
            }
	}
    },

    viewSelect:function(id) {
        let hiddenComp = GuiUtils.getDomObject(id + "_hidden");
        if (hiddenComp) {
	    let entryId = hiddenComp.obj.value;
	    if(Utils.stringDefined(entryId)) {
		let url = RamaddaUtils.getEntryUrl(entryId);
		window.open(url,'_blank');
	    }
	}
    },


    initEntryPopup:function(id,target) {
        let input = HU.input("","",["id",id+"_input",CLASS,"input","placeholder","Search", "style",
                                    HU.css("width","200px")]);
        input = HU.center(input);
        let html = input +HU.div([CLASS,"ramadda-select-search-results","id",id+"_results"]);
        $("#" +id).html(html);
        let results = $("#" + id +"_results");


        $("#" + id +"_input").keyup(function(event){
            let value =  $(this).val();
            if(value=="") {
                results.hide();
                results.html("");
                return;
            }
            let keycode = (event.keyCode ? event.keyCode : event.which);
            if(keycode == 13) {
                let searchLink =  ramaddaBaseUrl + "/search/do?text=" + encodeURIComponent(value) +"&output=json";
                results.html(HU.getIconImage(icon_wait) + " Searching...");
                results.show();
                let myCallback = {
                    entryListChanged: function(list) {
                        let entries = list.getEntries();
                        if(entries.length==0) {
                            results.show();
                            results.html("Nothing found");
                            return;
                        }
                        let html = "";
                        entries.forEach((entry,idx)=>{
                            html += HU.div(['index',idx, 'class','ramadda-clickable ramadda-entry'], entry.getIconImage() +" " + entry.getName());
                        });
                        results.html(html);
			results.find('.ramadda-entry').click(function() {
			    let entry = entries[$(this).attr('index')];
                            RamaddaUtils.selectClick(target, entry.getId(),entry.getName(),{
				entryName: entry.getName(),
				icon:entry.getIconUrl(),
				entryType:entry.getType().id
			    });
			});
			
                        results.show(400);
                    },
                    handleSearchError:function(url, error) {
                        results.html("An error occurred:" + error);
                    }
                };
                let entryList = new EntryList(getGlobalRamadda(), searchLink, myCallback, false);
                entryList.doSearch();
            }
        });
    },


    selectClick:function(id, entryId, value, opts) {
	selector = selectors[id];
	let handler = getHandler(id);
	if(!handler) handler = getHandler(selector.elementId);
	if (handler) {
            handler.selectClick(selector.selecttype, id, entryId, value);
            selector.cancel();
            return;
	}

	if (selector.selecttype == "wikilink") {
	    let args = {entryId: entryId,name:value};
	    if(opts) $.extend(args, opts);
            WikiUtil.insertAtCursor(selector.elementId, selector.textComp.obj,args);
	} else   if (selector.selecttype == "fieldname") {
            WikiUtil.insertAtCursor(selector.elementId, selector.textComp.obj,  value);
	} else   if (selector.selecttype == "image") {
            WikiUtil.insertAtCursor(selector.elementId, selector.textComp.obj,  "{{image entry=\"" + entryId +"\" caption=\"" + value+"\" width=400px align=center}} ");	
	} else if (selector.selecttype == "entryid") {
	    let editor = WikiUtil.getWikiEditor(selector.elementId);
	    if(editor) {
		editor.insertTags(entryId, " ", "importtype");
	    } else {
		if(selector.props && selector.props.callback) {
		    selector.props.callback(entryId,opts);
		} else {
		    WikiUtil.insertText(selector.elementId,entryId);
		}
	    }
	} else if (selector.selecttype == "entry:entryid") {
            WikiUtil.getWikiEditor(selector.elementId).insertTags("entry:" + entryId, " ", "importtype");
	} else {
            selector.getHiddenComponent().val(entryId);
            selector.getTextComponent().val(value);
	}
	selector.cancel();
    },


    getBaseUrl:function() {
	return ramaddaBaseUrl;
    },
    isRamaddaUrl:function(url) {
	return url.startsWith(ramaddaBaseUrl);
    },
    getUrl:function(url) {
	return RamaddaUtil.getBaseUrl()+url;
    },
    getCdnUrl:function(url) {
	return ramaddaCdn+url;
    },
    getEntryUrl:function(entryId) {
	return RamaddaUtil.getUrl("/entry/show?entryid=" + entryId);
    },

    fileDrops:{
    },
    doSave: function(entryId,authtoken,args, success,errorFunc) {
	args = args||{};
	args.entryid = entryId;
	args.authtoken = authtoken;
	args.response = "json";
	let url = RamaddaUtil.getUrl("/entry/change");
        $.post(url, args, (result) => {
	    if(success) {
		success(result);
	    }
	}).fail(error=>{
	    try {
		let json = JSON.parse(error.responseText);
		if(json.error)  {
		    if(error) {
			error(json.error);
		    }  else {
			alert("Error:" + json.error);
		    }
		    return;
		} else {
		}
	    } catch(err) {
	    }
	    if(errorFunc) {
		errorFunc(error.responseText);
	    } else {
		alert("Error:" + error.responseText);
	    }
	});
    },


    initEntryTable:function(id,opts,json) {
	let entryMap = {};
	if(Utils.isDefined(opts.details) && opts.details==false) {
	    opts.simple = true;
	}
	let simple =opts.simple;
	let dflt = !simple;
	let props =  {
	    actions:[],
	    showCrumbs:false,
	    showHeader:dflt,
	    showDate:dflt,
	    showCreateDate:dflt,	    
	    showSize:dflt,
	    showType:dflt,
	    showIcon:dflt,
	    showThumbnails:dflt,
	    showArrow:dflt,	    
	    showForm:dflt,	    
	}
	$.extend(props,opts);
	let entries = json.map((j,idx)=>{
	    let entry =  new Entry(j);
	    entryMap[entry.getId()]= entry;
	    return entry;
	});
	let html = "";
	let cols = [];
	cols.push({id:"name",label:"Name"});
	if(props.showDate)
	    cols.push({id:"fromdate",label:"Date",width:120});		    
	if(props.showCreateDate)
	    cols.push({id:"createdate",label:"Create Date",width:120});
	if(props.showSize)
	    cols.push({id:"size",label:"Size",width:100});
	if(props.showType)
	    cols.push({id:"type",label:"Type",width:120});		    			
	if(props.showHeader) {
	    html+="<table cellspacing=0 cellpadding=0 width=100% class=entry-list-header><tr>";
	    let hdrAttrs = ["class","entry-list-header-column ramadda-clickable"];
	    cols.forEach((col,idx)=> {
		let attrs = hdrAttrs;
		let width = col.width;
		if(idx==0 && props.showForm) {
		    html+=HU.td(['style','padding-left:3px;','width','10'],
				HU.div(['style','width:15px','id',id+'_formarrow', 'title','Click to show entry form', 'class','ramadda-clickable'], HU.getIconImage("fas fa-caret-right")));
		    width-=10;
		}
		if(Utils.isDefined(col.width)) {
		    attrs = Utils.mergeLists(attrs,["width",col.width]);
		}
		attrs = Utils.mergeLists(attrs,['orderby',col.id,'title','Sort by '+ col.label]);
		let v = col.label;
		if(col.id==props.orderby) {
		    if(Utils.isDefined(props.ascending)) {
			if(props.ascending)
			    v = HU.getIconImage('fas fa-arrow-up') + SPACE+v;
			else
			    v = HU.getIconImage('fas fa-arrow-down') +SPACE+v;
		    }
		}
		html+=HU.td(attrs,v);
	    });
	    html+="</table>";
	} else if(!simple) {
	    html+=HU.div(['class','entry-list-noheader']);
	}
	let innerId = Utils.getUniqueId();
	let tableId = Utils.getUniqueId();	
	let classPrefix  = simple?'entry-list-simple':'entry-list';

	let attrs = ['id',innerId,'class',classPrefix];
	html+=HU.open("div",attrs);
	let formId;
	if(props.showForm) {
	    formId = HU.getUniqueId('form_');
	    html+=HU.open('form',['id',formId,'method','post','action',RamaddaUtil.getUrl('/entry/getentries')]);
	    let form = HU.checkbox("",['style',HU.css('margin-left','3px'), 'title','Toggle all','id',id+'_form_cbx'],false);
	    let actions = [["","Apply action"]];
	    props.actions.forEach(action=>{
		actions.push([action.id,action.label]);
	    });
	    form+=SPACE1;
	    form+= HU.select("",['name','output','id',id+'_form_action'],actions);
	    form+=SPACE1;
	    form+=HU.open('input',['name','getselected','type','submit','value','Selected','class','submit ui-button ui-corner-all ui-widget','id','getselected1338','role','button']);
	    form+=SPACE1;
	    form+=HU.open('input',['name','getall','type','submit','value','All','class','submit ui-button ui-corner-all ui-widget','id','getall1337','role','button']);
	    /*
	    if(props.canDelete)
		form+=SPACE1+HU.span(['target-type','repository.delete','title','Shift-drag-and-drop entries to delete','class','ramadda-entry-target ramadda-clickable ramadda-hoverable'], HU.getIconImage('fas fa-trash'));
	    if(props.canExport)
	    form+=SPACE1+HU.span(['target-type','zip.export','title','Shift-drag-and-drop entries to export','class','ramadda-entry-target ramadda-clickable ramadda-hoverable',], HU.getIconImage('fas fa-file-export'));
	    */
	    html+=HU.div(['class',classPrefix +'-row','id',id+'_form','style','display:none;width:100%'],form);
	}

	let tableAttrs=['id',tableId];
	if(props.maxHeight) tableAttrs.push('style',HU.css('max-height',props.maxHeight,'overflow-y','auto'));
	html+=HU.open('div',tableAttrs);
	html+='</div>';
	html+='</form>';
	html+='</div>';
	let main = jqid(id);
	main.html(html);
	if(formId) {
	    jqid(formId).submit(( event ) =>{
		if(!Utils.stringDefined(jqid(id+'_form_action').val())) {
		    alert('No action specified');
		    event.preventDefault();
		}
	    });
	}

	main.find('.entry-list-header-column').click(function() {
	    let orderby = $(this).attr('orderby');
	    let url;
	    if(props.orderby == orderby) {
		//my orderby
		if(Utils.isDefined(props.ascending)) {
		    //if we have an ascending
		    if(props.ascending) 
			url = HU.addToDocumentUrl('ascending',false);
		    else {
			url = HU.addToDocumentUrl('ascending',null);
			url = HU.addToDocumentUrl('orderby',null);
		    }
		} else {
		    //add ascending
		    url = HU.addToDocumentUrl('orderby',orderby);
		    url = HU.addToDocumentUrl('ascending',true);
		}
	    } else {
		url = HU.addToDocumentUrl('ascending',true);	    		    
		url = HU.addToDocumentUrl('orderby',orderby);
	    }
            window.location = url;
	});

	//Don't do this as it screws up the width of the menu sometimes
	//	    HU.initSelect($("#"+id+"_form_action"));

	let formOpen = false;
	$('#'+ id+'_form_cbx').click(function() {
            let on = $(this).is(':checked');
	    jqid(id).find('.entry-form-select').prop('checked',on);
	});
	    
	let initFunc = (id)=>{
	    if(formOpen) {
		jqid(id).find('.entry-form-select').show();
	    }
	}

	$('#' + id+'_formarrow').click(function() {
	    formOpen = !formOpen;
	    if(formOpen) {
		jqid(id+'_form').show();
		jqid(id).find('.entry-form-select').show();
		$(this).html(HU.getIconImage("fas fa-caret-down"));
	    } else {
		jqid(id+'_form').hide();
		jqid(id).find('.entry-form-select').hide();
		$(this).html(HU.getIconImage("fas fa-caret-right"));
	    }
	});

	RamaddaUtil.showEntryTable(tableId,props,cols,id,entryMap,initFunc,entries);	
    },

    showEntryTable:function(id,props,cols,mainId,entryMap,initFunc,entries) {
	let main = $('#'+ mainId);
	let html = "";
	let space = "";
	let rowClass  = props.simple?'entry-list-simple-row':'entry-list-row entry-list-row-data';

	entries.forEach(entry=>{
	    let rowId = Utils.getUniqueId("row_");
	    let row =  HU.open('div',['entryid',entry.getId(),'id',rowId]);
	    row +=  HU.open('div',['entryid',entry.getId(),'id',rowId,'class',rowClass]);
	    let innerId = Utils.getUniqueId();
	    row+= HU.open('table',['cellspacing','0','cellpadding','border','width','100%','class',
				   'entry-row-table','entryid',entry.getId()]);
	    row+='<tr valign=top>';
	    cols.forEach((col,idx)=> {
		let last = idx==cols.length-1;
		let attrs = [];
		let v = entry.getProperty(col.id);
		let title = null;
		if(col.id=="name") {
		    if(props.showIcon) {
			v = entry.getIconImage()+SPACE +v;
		    }
		    v = entry.getLink(v);
		    let tds = [];
		    //[cbx,space,arrow,icon,thumbnail,v]
		    let cbxId = Utils.getUniqueId('entry_');
		    if(props.showForm)
			tds.push(HU.hidden("allentry",entry.getId()) +
				 HU.checkbox(cbxId,['rowid',rowId,'name','selentry','value', entry.getId(),'class','entry-form-select','style',HU.css('margin-right','2px','display','none')],false));

		    tds.push(HU.div(['style',HU.css('min-width','10px'),'innerid',innerId,'entryid',entry.getId(),'title','Click to show contents','class','entry-arrow ramadda-clickable' ], HU.getIconImage("fas fa-caret-right")));


		    if(props.showThumbnails) {
			let thumbnail = entry.getThumbnail();
			if(thumbnail)
			    tds.push(HU.div(['class','ramadda-thumbnail','style',HU.css('max-height','100px','overflow-y','auto')], HU.image(thumbnail,['loading','lazy','class','ramadda-clickable ramadda-thumbnail-image','title','Click to enlarge',
															'style',HU.css('width','100px')])));
		    }
		    if(props.showCrumbs && entry.breadcrumbs) {
			let crumbId = Utils.getUniqueId();
			v = HU.span(['id','breadcrumbtoggle_' + crumbId, 'breadcrumbid',crumbId, 'title','Show breadcrumbs','class','ramadda-clickable ramadda-breadcrumb-toggle' ], HU.getIconImage("fas fa-plus-square")) +SPACE2
			    + HU.span(['style',HU.css('display','none'),'id',crumbId], entry.breadcrumbs+"&nbsp;&raquo;&nbsp;") +v;
		    }

		    tds.push(v);
		    if(!props.simple)
			title = 'Right-click to see entry menu. Shift-drag to copy/move';
		    v =  HU.table([],HU.tr(['valign','top'],HU.tds([],tds)));
		} else {
		    if(col.id=="type") {
			v = HU.href(RamaddaUtil.getUrl("/search/type/" + entry.getType().id),v,["title","Search for entries of type " + v]);
		    }
		    let maxWidth = col.width-20;
		    maxWidth = col.width;		    
		    v = HU.div(['style',HU.css('padding-left','5px','white-space','nowrap','width',HU.getDimension(col.width),'text-align','right','max-width',HU.getDimension(maxWidth),'overflow-x','hidden')+(last?HU.css('padding-right','4px'):'')],v);
		    attrs.push("align","right");
		}
		if(Utils.isDefined(col.width)) {
		    attrs.push("width",HU.getDimension(col.width));
		}
		if(title) {
		    attrs.push('title',title);

		}
//		v = HU.div(['class','entry-row-label'], v);

		row+=HU.td(attrs,v);
	    });		
	    row+="</tr></table>";
	    row+="</div>"
	    row+=HU.div(['id',innerId,'style',HU.css('margin-left','20px')]);
	    row+="</div>";
	    html+=row;
	});
	html+=HU.close('div');

	$('#'+id).html(html);
	$('#'+id).find('.ramadda-breadcrumb-toggle').click(function() {
	    let id = $(this).attr('breadcrumbid');
	    let crumbs = $('#'+ id);
	    if(crumbs.css('display')=='none') {
		$('#breadcrumbtoggle_' +id).html(HU.getIconImage("fas fa-minus-square"));
		crumbs.css('display','inline');
	    } else {
		$('#breadcrumbtoggle_' +id).html(HU.getIconImage("fas fa-plus-square"));
		crumbs.css('display','none');
	    }
	});

	$('#'+ id).find('.entry-arrow').click(function() {
	    let entryId = $(this).attr('entryid');
	    let innerId = $(this).attr('innerid');	    
	    let inner = $("#"+innerId);
	    let filled = $(this).attr("filled");
	    let open = $(this).attr("open");
	    if(open) {
		$(this).html(HU.getIconImage("fas fa-caret-right"));
	    } else {
		$(this).html(HU.getIconImage("fas fa-caret-down"));
	    }
	    if(filled) {

		if(open) {
		    inner.hide();
		    $(this).attr('open',false);	    
		} else {
		    inner.show();
		    $(this).attr('open',true);	    
		}
		return;
	    }
	    let entry = entryMap[entryId];
	    $(this).attr('filled',true);
	    $(this).attr('open',true);	    
	    let url = RamaddaUtil.getUrl('/entry/show?output=json&includeproperties=false&includedescription=false&includeservices=false&children=true&entryid='+entryId);
            $.getJSON(url, function(data, status, jqxhr) {
                if (GuiUtils.isJsonError(data)) {
                    return;
                }
		let entries = data.map((j,idx)=>{
		    let entry=  new Entry(j);
		    entryMap[entry.getId()]= entry;
		    return entry;
		});
		if(entries.length>0) {
		    RamaddaUtil.showEntryTable(innerId,props,cols,mainId, entryMap,initFunc,entries);	
		} else {
		    let table = HU.open('table',['class','formtable']);
		    if(entry.getIsUrl()) {
			table+=HU.formEntry('URL:',HU.href(entry.getResourceUrl(),entry.getResourceUrl()));
		    } else if(entry.getIsFile()) {
			let url = entry.getResourceUrl();
			table+=HU.formEntry('File:',HU.href(url,entry.getFilename()) +' ' +
					    ' (' + entry.getFormattedFilesize()+')' +
					    ' ' + HU.href(url,HU.getIconImage('fas fa-download')));
		    }
		    
		    table+=HU.formEntry('Kind:',HU.href(RamaddaUtil.getUrl('/search/type/' + entry.getType().id),entry.typeName,['title','Search for entries of type ' + entry.typeName]));
		    let searchUrl = RamaddaUtil.getUrl('/search/type/' + entry.getType().id+'?user_id='+ entry.creator+'&search.submit=true');
		    let created = HU.href(searchUrl,entry.creator,
					  ['title','Search for entries of this type created by ' + entry.creator]);
		    table+=HU.formEntry('Created by:',created);
		    table+=HU.formEntry('Created:',entry.createDateFormat);
		    if(entry.startDate && entry.startDate.getTime()!=entry.createDate.getTime())
			table+=HU.formEntry('Date:',entry.startDateFormat);
		    if(entry.startDate && entry.endDate && entry.startDate.getTime()!=entry.endDate.getTime()) {
			console.log(entry.startDate);
			console.log(entry.endDate);
			table+=HU.formEntry('To Date:',entry.endDateFormat);
		    }
		    table+='</table>';
		    HU.jqid(innerId).html(table);
		}
	    });
	});

	if(props.simple) return;
	$('#'+ id).find('.entry-form-select').click(function(event) {
            let on  = $(this).is(':checked');
	    let row = $('#' + $(this).attr('rowid'));
	    HU.checkboxClicked(event,'entry_',$(this).attr('id'));
	    main.find('.entry-form-select').each(function() {
		let on  = $(this).is(':checked');
		let row = $('#' + $(this).attr('rowid'));
		if(on) row.addClass('entry-list-row-selected');
		else row.removeClass('entry-list-row-selected');	    
	    });
	});

	$('#'+ id).find('.ramadda-thumbnail-image').click(function() {
	    let src = $(this).attr('src');	
	    HU.makeDialog({content:HU.image(src),my:'left top',at:'left bottom',anchor:this,header:true,draggable:true});
	});

	let rows = $('#'+id).find('.entry-list-row');
	rows.bind ('contextmenu', function(event) {
	    let entryRow = $(this);
	    let entry = entryMap[$(this).attr('entryid')];
	    if(!entry) return;
            eventX = GuiUtils.getEventX(event);
            let url = RamaddaUtil.getUrl("/entry/show?entryid=" + entry.getId() + "&output=metadataxml");
	    let handleTooltip = function(request) {
		let xmlDoc = request.responseXML.documentElement;
		text = getChildText(xmlDoc);
		HU.makeDialog({content:text,my:'left top',at:'left bottom',title:entry.getIconImage()+" "+entry.getName(),anchor:entryRow,header:true});
	    }
            GuiUtils.loadXML(url, handleTooltip, this);
	    if (event.preventDefault) {
		event.preventDefault();
	    } else {
		event.returnValue = false;
		return false;
	    }
	});

	RamaddaUtil.initDragAndDropEntries(rows,entryMap,mainId);
	initFunc(id);


    },
    

    initDragAndDropEntries:function(rows, entryMap,mainId) {
	rows.mousedown(function(event) {
            if (!event.shiftKey || !entryMap) return;
	    let entry = entryMap[$(this).attr('entryid')];
	    if(!entry) return;
	    let source = $(this);
	    let entries = [entry];
	    $('#'+mainId).find('.entry-list-row-selected').each(function() {
		let selectedEntry = entryMap[$(this).attr('entryid')];
		if(!selectedEntry) return;
		if(selectedEntry.getId()!=entry.getId()) {
		    entries.push(selectedEntry);
		}
	    });
	    Utils.entryDragInfo = {
		dragSource: source.attr('id'),
		entry:entry,
		getIds: function() {
		    return entries.map(entry=>{return entry.getId();}).join(",");
		},
		hasEntry:function(entry) {
		    return entries.includes(entry);
		},
		getHtml:function() {
		    let html = "";
		    entries.forEach(entry=>{
			if(html!="") html+="<br>";
			html+=  HU.image(entry.getIconUrl()) + SPACE +entry.getName();
		    });
		    return html;
		},
	    }
	    if(entry) {
		Utils.mouseIsDown = true;
		if (event.preventDefault) {
		    event.preventDefault();
		} else {
		    event.returnValue = false;
		    return false;
		}
	    }});


	function isTarget(comp) {
	    return comp.hasClass('ramadda-entry-target');
	}

	rows.mouseover(function(event) {
	    let bg  = "#C6E2FF";
	    if(isTarget($(this))) {
		if (Utils.mouseIsDown && Utils.entryDragInfo) {
		    $(this).css("background", bg);
		}
		return
	    } 
	    let entry = entryMap[$(this).attr('entryid')];
	    if(!entry) return;
	    if (Utils.mouseIsDown && Utils.entryDragInfo) {
		if(Utils.entryDragInfo.hasEntry(entry)) return;
		$(this).css("background", bg);
	    }
	});

	rows.mouseout(function(event) {
	    if(Utils.entryDragInfo) {
		if(isTarget($(this))) {
		    $(this).css("background", "");
		    return
		}

		let entry = entryMap[$(this).attr('entryid')];
		if(Utils.entryDragInfo.entry == entry) return;
		$(this).css("background", "");
	    }

	});

	rows.mouseup(function(event) {
	    if(!Utils.entryDragInfo) return;
	    $(this).css("background", "");
	    if(isTarget($(this))) {
		let url =  RamaddaUtil.getUrl('/entry/getentries?output=' + $(this).attr('target-type'));
		Utils.entryDragInfo.getIds().split(',').forEach(id=>{
		    url+='&selentry=' + id;
		});
		document.location = url;
		return;
	    }

	    let entry = entryMap[$(this).attr('entryid')];
	    if(!entry) return;
	    if(Utils.entryDragInfo.hasEntry(entry)) return;
	    if(!Utils.entryDragInfo.hasEntry(entry)) {
		url = RamaddaUtil.getUrl("/entry/copy?action=action.move&from=" + Utils.entryDragInfo.getIds() + "&to=" + entry.getId());
		document.location = url;
	    }
	});
    },

    initFormTags: function(formId) {
	let form = $('#'+formId);
	let inputs = form.find('.metadata-tag-input');
	form.attr('autocomplete','off');
	inputs.attr('autocomplete','off');
	inputs.keyup(function(event) {
	    HtmlUtils.hidePopupObject();
	    let val = $(this).val();
	    if(val=="") return;
	    let url = HU.getUrl(RamaddaUtil.getUrl("/metadata/suggest"),["value",val.trim()]);
	    let input = $(this);
	    $.getJSON(url, data=>{
		if(data.length==0) return;
		let suggest = "";
		data.forEach(d=>{
		    suggest+=HU.div([CLASS,"ramadda-clickable metadata-suggest","suggest",d],d);
		});
		let html = HU.div([CLASS,"ramadda-search-popup",STYLE,HU.css("max-width","200px",
									     "padding","4px")],suggest);
		let dialog = HU.makeDialog({content:html,my:"left top",at:"left bottom",anchor:input});
		dialog.find(".metadata-suggest").click(function() {
		    HtmlUtils.hidePopupObject();
		    input.val($(this).attr("suggest"));
		});
	    }).fail(
		    err=>{console.log("url failed:" + url +"\n" + err)});
	});

	let tags = $('#'+formId).find('.metadata-tag');
	tags.attr('title','Click to remove');
	tags.click(function() {
	    let input = form.find("#" + $(this).attr('metadata-id'));
	    if($(this).hasClass('metadata-tag-deleted')) {
		$(this).removeClass('metadata-tag-deleted')		
		input.val("");
	    } else {
		$(this).addClass('metadata-tag-deleted')		
		input.val("delete");
	    }
	});
    },
    initFormUpload:function(fileInputId, targetId) {
	let input = $("#"+ fileInputId);
	let form = input.closest('form');
	let custom = HU.div([TITLE,"Click to select a file", ID,fileInputId+"_filewrapper",CLASS, 'fileinput_wrapper'],HU.getIconImage("fas fa-cloud-upload-alt") +" " +HU.div([ID,fileInputId+"_filename",CLASS,"fileinput_label"]));
	input.after(custom);
	input.hide();
	let inputWrapper = $("#" +fileInputId+"_filewrapper");
	let inputLabel = $("#" +fileInputId+"_filename");
	inputWrapper.click(()=>{input.trigger('click');});
	let inputChanger = ()=>{
	    let file  = input[0].files? input[0].files[0]:null;
	    let fileName =  file?file.name:input.val(); 
	    let idx = fileName.lastIndexOf("\\");
	    if(idx<0)
		idx = fileName.lastIndexOf("/");		
	    if(idx>=0) {
		fileName = fileName.substring(idx+1);
	    }
	    if(file) {
		fileName = fileName +" (" + Utils.formatFileLength(file.size)+")";
	    }
	    if(fileName=="") fileName = HU.span([CLASS,"fileinput_label_empty"],"Please select a file");
	    $('#' + fileInputId+"_filename").html(fileName); 
	};
	input.bind('change', inputChanger);
	inputChanger();

	if(Utils.stringDefined(targetId)) {
	    let target = $("#" + targetId);
	    let fileDrop = {
		files:{},
		cnt:0,
		added:false
	    };
	    target.append(HU.div([CLASS,"ramadda-dnd-target-files",ID,fileInputId+"_dnd_files"]));
	    let files=$("#" +fileInputId+"_dnd_files");
	    Utils.initDragAndDrop(target,
				  event=>{},
				  event=>{},
				  (event,item,result,wasDrop) =>{
				      fileDrop.cnt++;
				      let name = item.name;
				      if(!name) {
					  let isImage = item.type && item.type.match("image/.*");
					  name = prompt("Entry file name:",isImage?"image":"file");
					  if(!name) return;
					  if(item.type) {
					      if(item.type=="text/plain") {
						  if(!name.endsWith(".txt")) {
						      name = name+".txt";
						  }
					      } else {
						  let type  = item.type.replace(/.*\//,"");
						  name = name+"."+type;
					      }
					  }
				      }
				      let listId = fileInputId +"_list" + fileDrop.cnt;
				      let inputId = fileInputId +"_file" + fileDrop.cnt;
				      let nameInputId = fileInputId +"_file_name" + fileDrop.cnt;
				      let fileName = "upload_file_" + fileDrop.cnt;
				      let nameName = "upload_name_" + fileDrop.cnt;				  				  
				      fileDrop.files[inputId] = result;
				      let del =HU.span([CLASS,"ramadda-clickable",ID,listId+"_trash"],HU.getIconImage(icon_trash));
				      let size = Utils.isDefined(item.size)?Utils.formatFileLength(item.size):"";
				      files.append(HU.div([ID,listId],del +" " +name+" "+size));
				      form.append(HU.tag("input",['type','hidden','name',fileName,'id',inputId]));
				      form.append(HU.tag("input",['type','hidden','name',nameName,'id',nameInputId]));				  
				      $("#"+inputId).val(result);
				      $("#"+nameInputId).val(name);				  
				      $("#"+listId+"_trash").click(function(){
					  $("#"+listId).remove();
					  $("#"+inputId).remove();
					  $("#"+nameInputId).remove();				      
				      });
				  },null, true);
	}			      
    },

    handleDropEvent:function(event,file, result,entryId,callback) {
	let isImage= file.type.match('^image.*');
	let url = RamaddaUtil.getUrl("/entry/addfile");
	let desc = "";
	let name = file.name;
	if(!name) {
	    name = prompt("Entry Name:");
	    if(!name) return;
	}

	let fileName = file.name;
	let suffix = file.type.replace(/image\//,"");
	if(!fileName) {
	    fileName =  name+"." + suffix;
	}
	let data = new FormData();
	data.append("filename",fileName);
	//A hack for shapefiles and geojson
	if(file.type=='application/zip') 
	    data.append("filetype",'geo_shapefile');
	else if(file.type=='application/json') 
	    data.append("filetype",'geo_geojson');	
	else
	    data.append("filetype",file.type);
	data.append("group",entryId);
	data.append("description",desc);
	data.append("file", result);
	let dialog;
	$.ajax({
	    url: url,
	    cache: false,
	    contentType: false,
	    processData: false,
	    method: 'POST',
	    type: 'POST', 
	    data: data,
	    success:  (data) =>{
		dialog.remove();
		if(data.status!='ok') {
		    alert("An error occurred creating entry: "  + data.message);
		    return;
		}
		if(callback) callback(data,data.entryid, data.name,isImage);
	    },
	    error: function (err) {
		dialog.remove();
		alert("An error occurred creating entry: "  + err);
	    }
	});
	let html = HU.div(['style',HU.css('text-align','center','padding','5px')], "Creating entry<br>"+HU.image(ramaddaCdn + '/icons/mapprogress.gif',['width','50px']));
	dialog = HU.makeDialog({content:html,anchor:$(document),my:"center top",at:"center top+100"});    
    },


    //applies extend to the given object
    //and sets a super member to the original object
    //you can call original super class methods with:
    //this.super.<method>.call(this,...);
    inherit: function(object, parent) {
        $.extend(object, parent);
        parent.getThis = function() {
            return object;
        }
        object.getThis = function() {
            return object;
        }
        object.mysuper = parent;
        return object;
    },
    //Just a wrapper around extend. We use this so it is easy to find 
    //class definitions
    initMembers: function(object, members) {
        $.extend(object, members);
        return object;
    },
    //Just a wrapper around extend. We use this so it is easy to find 
    //class definitions
    defineMembers: function(object, members) {
        $.extend(object, members);
        return object;
    },

    showEntryPopup:function(id,entryId,label) {
	let html = RamaddaUtils.contents[entryId];
	if(html) {
	    RamaddaUtils.showEntryPopupInner(id,entryId,label,html);
	} else {
	    let url = RamaddaUtil.getUrl("/entry/menu?entryid=" + entryId);
            $.ajax({
                url: url,
                dataType: 'text',
                success: function(html) {
		    RamaddaUtils.contents[entryId] = html;
		    RamaddaUtils.showEntryPopupInner(id,entryId,label,html);
                }
            }).fail((jqxhr, settings, exc) => {
                console.log("/entry/menu failed:" + exc);
		alert('Failed to contact the server');
            });
	}
    },

    showEntryPopupInner:function(id,entryId,label,html) {
	let anchor = $("#" + id);
	let headerRight=null;
	/*
	if(Utils.isAnonymous()) {
	headerRight = HU.href('/repository/user/login',
	HU.getIconImage('fas fa-sign-in-alt')+' Login' + HU.space(2));
	}
	*/

	HU.makeDialog({content:html,my:"left top",at:"left bottom",title:label,anchor:anchor,draggable:true,header:true,inPlace:false,headerRight:headerRight});    
    },

    initEntryListForm:function(formId) {
	let visibilityGroup = Utils.entryGroups[formId];
	if (visibilityGroup) {
            visibilityGroup.on = 0;
            visibilityGroup.setVisbility();
	}
    },

    hideEntryPopup:function() {
	HtmlUtils.getTooltip().hide();
    },




    originalImages:new Array(),
    changeImages: new Array(),

    folderClick:function(uid, url, changeImg) {
	console.log(url);
	RamaddaUtil.changeImages[uid] = changeImg;
	let jqBlock = $("#" + uid);
	if (jqBlock.length == 0) {
            return;
	}
	let jqImage = $("#img_" + uid);
	let showing = jqBlock.css('display') != "none";
	if (!showing) {
            RamaddaUtil.originalImages[uid] = jqImage.html();
            jqBlock.show();
            jqImage.html(HU.getIconImage("fa-caret-down"));
	    url +="&orderby=entryorder&ascending=true";
	    if(url.startsWith("/") && RamaddaUtil.currentRamaddaBase) {
		url = RamaddaUtil.currentRamaddaBase +url;
	    }

            GuiUtils.loadXML(url, RamaddaUtil.handleFolderList, uid);
	} else {
            if (changeImg) {
		jqImage.html(HU.getIconImage("fa-caret-right"));
	    }
            jqBlock.hide();
	}
    },

    handleFolderList:function(request, uid) {
	if (request.responseXML != null) {
            let xmlDoc = request.responseXML.documentElement;
            let script;
            let html;
            for (i = 0; i < xmlDoc.childNodes.length; i++) {
		let childNode = xmlDoc.childNodes[i];
		if (childNode.tagName == "javascript") {
                    script = getChildText(childNode);
		} else if (childNode.tagName == "content") {
                    html = getChildText(childNode);
		} else {}
            }
            if (!html) {
		html = getChildText(xmlDoc);
            }
            if (html) {
		$("#" + uid).html("<div>" + html + "</div>");
		Utils.checkTabs(html);
            }
            if (script) {
		eval(script);
            }
	}
	
	if (RamaddaUtil.changeImages[uid]) {
            $("#img_" + uid).attr('src', icon_folderopen);
	} else {
            $("#img_" + uid).attr('src', RamaddaUtil.originalImages[uid]);
	}
    },


    Components: {
	init: function(id) {
	    let container = $("#" + id);
	    let header = $("#" + id +"_header");
	    let hasTags = false;
	    let hasLocations = false;
	    let components = container.find(".ramadda-component");
	    let years = {};	    
	    let months = {};
	    let days = {};	    
	    components.each(function() {
		let date = $(this).attr("component-date");
		if(!date) return;
		let dttm = Utils.parseDate(date)
		let tmp;
		$(this).attr("component-day",tmp = Utils.formatDateWithFormat(dttm,"mmmm d yyyy"));
		days[tmp]  =true;
		$(this).attr("component-month",tmp = Utils.formatDateWithFormat(dttm,"mmmm yyyy"));
		months[tmp]  =true;
		$(this).attr("component-year",tmp = Utils.formatDateWithFormat(dttm,"yyyy"));		
		years[tmp] = true;
		if($(this).attr('component-latitude')) hasLocations = true;
		if(Utils.stringDefined($(this).attr('component-tags'))) {
		    hasTags = true;
		}
	    });
	    header.css("text-align","center");
	    let hdr = 
		HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar ramadda-button-on","layout","grid"],"Grid");
	    if(Object.keys(years).length>1)
		hdr += HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","year"],"Year");
	    if(Object.keys(months).length>1)
		hdr+=HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","month"],"Month");
	    if(Object.keys(days).length>1)	    
		hdr+=HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","day"],"Day");		
	    hdr += HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","title"],"Title");	    
	    if(hasTags)
		hdr +=HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","tags"],"Tag");

	    hdr+= HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","author"],"Author");			
	    if(hasLocations) {
		hdr += HU.div([STYLE,HU.css("display","inline-block"), CLASS,"ramadda-button ramadda-button-bar","layout","map"],"Map");			
	    }
	    header.append(HU.div([],hdr));


	
	    let buttons = header.find(".ramadda-button");
	    buttons.click(function(){
		buttons.removeClass("ramadda-button-on");
		$(this).addClass("ramadda-button-on");	    
		let layout = $(this).attr("layout");
		if(layout=="grid") RamaddaUtil.Components.layout(container,components,null);
		else  RamaddaUtil.Components.layout(container,components,"component-" + layout);
	    });
	},
	layout: function(container,components,by) {
	    container.find(".ramadda-group").each(function() {
		$(this).detach();
	    });
	    if(container.mapId)
		$("#"+ container.mapId).detach();
	    if(container.ramaddaMap) {
		ramaddaMapRemove(container.ramaddaMap);
		container.ramaddaMap = null;
	    }
	    if(by==null) {
		components.show();
		components.each(function() {
		    $(this).detach();
		    container.append($(this));
		});
		return;
	    } else if(by=='component-map') {
		components.hide();
		let id = Utils.getUniqueId();
		container.mapId = id;
		container.append(HU.div([ID,id,STYLE,HU.css("width","100%","height","400px")]));
		let params={};
		let map = new RepositoryMap(id,params);
		container.ramaddaMap = map;
		map.initMap(false);
		components.each(function() {
		    let  url = $(this).attr("component-url");		    
		    let lat = +$(this).attr("component-latitude");
		    let lon = +$(this).attr("component-longitude");		    
		    if(!Utils.isNumber(lat)) return;
		    let image = $(this).attr("component-image");		    
		    let popup = HU.center(HU.b($(this).attr("component-title")));		    
		    if(image) popup +=HU.image(image,[WIDTH,"300px"]);
		    if(url) popup=HU.href(url,popup);
		    let point = new MapUtils.createLonLat(lon,lat);
		    map.addPoint("", point, {pointRadius:6, strokeWidth:1, strokeColor:'#000', fillColor:"blue"},popup);
		});
		map.centerOnMarkers();
	    } else {
		components.show();

		let isDate = 		by=="component-day" || by=="component-month" || by=="component-year";
		let values = [];
		let valueMap = {};
		components.each(function() {
		    let attr = $(this).attr(by)||"";
		    if(by=="component-tags") {
			let tags = attr.split(",");
			attr = tags[0];
		    }
		    if(!valueMap[attr]) {
			valueMap[attr] = [];
			let dttm = null;
			if(isDate) {
			    let date = $(this).attr("component-date");
			    let dttm = date?Utils.parseDate(date):null;
			    values.push([attr,dttm]);
			} else {
			    values.push(attr);
			}
		    }
		    valueMap[attr].push($(this));
		});
		if(isDate) {
		    values = values.sort((a,b)=>{
			a = a[1];
			b = b[1];
			if(!a || !b) return 0;
			if(!a) return 1;
			if(!b) return -1;
			return b.getTime()-a.getTime();
		    });
		} else {
		    values = values.sort();
		}
		values.forEach(value=>{
		    if(isDate) value = value[0];
		    let group = container.append($(HU.div([CLASS,"ramadda-group"],HU.div([CLASS,"ramadda-group-header"],value))));
		    valueMap[value].forEach(child=>{
			group.append(child);
		    })
		});


	    }
   
	    
	},
    }

    
}



function EntryRow(entryId, rowId, cbxId, cbxWrapperId, showDetails,args) {
    if(!args) args = {
	showIcon:true
    } 
    this.args = args;
    args.entryId = entryId;
    Utils.globalEntryRows[entryId] = this;
    Utils.globalEntryRows[rowId] = this;
    Utils.globalEntryRows[cbxId] = this;
    this.entryId = entryId;
    this.showIcon = args.showIcon;
    this.onColor = "#FFFFCC";
    this.overColor = "#f4f4f4";
    this.rowId = rowId;
    this.cbxId = cbxId;
    this.cbxWrapperId = cbxWrapperId;
    this.showDetails = showDetails;
    this.getRow = function() {
        return $("#" + this.rowId);
    }

    this.getCbx = function() {
        return $("#" + this.cbxId);
    }

    let form = this.getCbx().closest('form');
    if (form.length) {
        let visibilityGroup = Utils.entryGroups[form.attr('id')];
        if (visibilityGroup) {
            visibilityGroup.addEntryRow(this);
        }
    } else {
        this.getCbx().hide();
    }

    this.setCheckbox = function(value) {
        this.getCbx().prop('checked', value);
        this.setRowColor();
    }


    this.isSelected = function() {
        return this.getCbx().is(':checked');
    }

    this.setRowColor = function() {
        this.getRow().removeClass("entry-row-hover");	    
        if (this.isSelected()) {
            this.getRow().addClass("entry-list-row-selected");
        } else {
            this.getRow().removeClass("entry-list-row-selected");	    
        }
    }


    this.mouseOver = function(event) {
        $("#" + "entrymenuarrow_" + rowId).attr('src', icon_menuarrow);
        this.getRow().addClass("entry-row-hover");
//        this.getRow().css('background-color', this.overColor);
    }

    this.mouseOut = function(event) {
        $("#entrymenuarrow_" + rowId).attr('src', icon_blank);
        this.setRowColor();
    }


    this.mouseClick = function(event) {
        eventX = GuiUtils.getEventX(event);
        let position = this.getRow().offset();
        //Don't pick up clicks on the left side
        if (eventX - position.left < 150) return;
        this.lastClick = eventX;
        let url = RamaddaUtil.getUrl("/entry/show?entryid=" + entryId + "&output=metadataxml");
        if (this.showDetails) {
            url += "&details=true";
        } else {
            url += "&details=false";
        }
        if (this.showIcon) {
            url += "&showIcon=true";
        } else {
            url += "&showIcon=false";
        }
        GuiUtils.loadXML(url, this.handleTooltip, this);
    }

    this.handleTooltip = function(request, entryRow) {
        let xmlDoc = request.responseXML.documentElement;
        text = getChildText(xmlDoc);
        let leftSide = entryRow.getRow().offset().left;
        let offset = entryRow.lastClick - leftSide;
        let close = HU.jsLink("", HU.getIconImage(icon_close), ["onmousedown", "RamaddaUtil.hideEntryPopup();","id","tooltipclose"]);
	let label = HU.image(entryRow.args.icon)+ SPACE +entryRow.args.name;
	let header =  HU.div([CLASS,"ramadda-popup-header"],close +SPACE2 +label);
	let html = HU.div([CLASS,"ramadda-popup",STYLE,"display:block;"],   header + "<table>" + text + "</table>");
	let popup =  HtmlUtils.getTooltip();
	popup.html(html);
	popup.draggable();
        Utils.checkTabs(text);
        let pos = entryRow.getRow().offset();
        let eWidth = entryRow.getRow().outerWidth();
        let eHeight = entryRow.getRow().outerHeight();
        let mWidth = HtmlUtils.getTooltip().outerWidth();
        let wWidth = $(window).width();

        let x = entryRow.lastClick;
        if (entryRow.lastClick + mWidth > wWidth) {
            x -= (entryRow.lastClick + mWidth - wWidth);
        }

        let left = x + "px";
        let top = (3 + pos.top + eHeight) + "px";
        popup.css({
            position: 'absolute',
            zIndex: 5000,
            left: left,
            top: top
        });
        popup.show();
    }
}


var selectors = new Array();
function Selector(event, selectorId, elementId, allEntries, selecttype, localeId, entryType, ramaddaUrl,props) {
    let _this = this;
    this.id = selectorId;
    this.domId = HU.getUniqueId('selector_');

    this.props = props||{};
    this.elementId = elementId;
    this.localeId = localeId;
    this.entryType = entryType;
    this.allEntries = allEntries;
    this.selecttype = selecttype;
    this.textComp = GuiUtils.getDomObject(this.elementId);
    this.ramaddaUrl = ramaddaUrl || ramaddaBaseUrl;
    this.getTextComponent = function() {
        let id = "#" + this.elementId;
        return $(id);
    }

    this.getHiddenComponent = function() {
        let id = "#" + this.elementId + "_hidden";
        return $(id);
    }

    this.cancel= function(override) {
	if(!override) {
	    if(this.pinned) return;
	}
	jqid(this.domId).remove();
    }

    this.clearInput = function() {
        this.getHiddenComponent().val("");
        this.getTextComponent().val("");
    }

    this.handleClick = function(event) {
        let src = this.props.anchor;
	if(!src) {
	    if(this.props.eventSourceId) {
		src = jqid(this.props.eventSourceId);
	    }
	    if(src==null || src.length==0) {
		if(event && event.target) {
		    src = $(event.target);
		}
	    }
	    if(src==null || src.length==0)  {
		let srcId = this.id + '_selectlink';
		src = jqid(srcId);
	    }
	    if(src==null || src.length==0)  {
		src = jqid(this.id);
	    }
	}

        HtmlUtils.hidePopupObject(event);
	let container = $(HU.div(['style','position:relative;'])).appendTo("body");
        $(HU.div(['style',HU.css('min-width','200px','min-height','200px'),'class','ramadda-selectdiv','id',this.domId])).appendTo(container);
        this.div = jqid(this.domId);
	if(this.props.minWidth) {
	    this.div.css('min-width',this.props.minWidth);
	}
	this.div.draggable();
	this.anchor = src;
	this.showDiv = () =>{
            this.div.show();
            this.div.position({
		of: this.anchor,
		my: this.props.locationMy??"left top",
		at: this.props.locationAt??"left bottom",
		collision: this.props.collision??"fit fit"
            });
	};


        let url =  "/entry/show?output=selectxml&selecttype=" + this.selecttype + "&allentries=" + this.allEntries + "&target=" + this.id + "&noredirect=true&firstclick=true";

	if(this.ramaddaUrl && !this.ramaddaUrl.startsWith("/")) {
	    let pathname = new URL(this.ramaddaUrl).pathname
	    let root = this.ramaddaUrl.replace(pathname,"");
	    RamaddaUtil.currentRamaddaBase = root;
            url = this.ramaddaUrl + url;
	} else {
	    RamaddaUtil.currentRamaddaBase = null;
	    url = RamaddaUtil.getUrl(url);
	}
        if (this.localeId) {
            url = url + "&localeid=" + this.localeId;
        }
        if (this.entryType) {
            url = url + "&entrytype=" + this.entryType;
        }
	if(this.props.typeLabel) {
            url = url + "&typelabel=" + this.props.typeLabel;
	}
        GuiUtils.loadXML(url, (request,id)=>{_this.handleSelect(request,id)}, this.id);
        return false;
    }
    this.handleClick(event);
}


Selector.prototype = {
    handleSelect:function(request, id) {
	let _this = this;
	let xmlDoc = request.responseXML.documentElement;
	text = getChildText(xmlDoc);
	let pinId = this.domId +"-pin";
	let pin = HU.jsLink("",HtmlUtils.getIconImage(icon_pin), ["class","ramadda-popup-pin", "id",pinId]); 
	let closeImage = HtmlUtils.getIconImage(icon_close, []);
	let closeId = id+'_close';
	let close = HU.span(['id',closeId,'class','ramadda-clickable'],closeImage);
	let title = (this.props?this.props.title:"")??"";
	let extra = (this.props?this.props.extra:"")??"";
	if(Utils.stringDefined(title)) {
	    title = HU.span(['style','margin-left:5px;'], title);
	}
	let header = HtmlUtils.div(["style","text-align:left;","class","ramadda-popup-header"],SPACE+close+SPACE+pin+title);
	let popup = HtmlUtils.div(["id",id+"-popup"], header + extra+text);
	this.div.html(popup);
	this.showDiv();
	
	jqid(closeId).click(()=>{
	    this.cancel(true);
	});
	$("#" + pinId).click(function() {
	    _this.pinned = !_this.pinned;
	    if(!_this.pinned) {
		$(this).removeClass("ramadda-popup-pin-pinned");
	    } else {
		$(this).addClass("ramadda-popup-pin-pinned");
	    }
	});
	/*
	let arr = this.div.getElementsByTagName('script')
	//Eval any embedded js
	for (let n = 0; n < arr.length; n++) {
	    eval(arr[n].innerHTML);
	}
	*/
	if(this.props && this.props.initCallback) {
	    this.props.initCallback();
	}
    }
}












function getChildText(node) {
    let text = '';
    for (childIdx = 0; childIdx < node.childNodes.length; childIdx++) {
        text = text + node.childNodes[childIdx].nodeValue;
    }
    return text;
}


function toggleBlockVisibility(id, imgid, showimg, hideimg) {
    HtmlUtils.toggleBlockVisibility(id, imgid, showimg, hideimg);
}


function toggleInlineVisibility(id, imgid, showimg, hideimg) {
    let img = GuiUtils.getDomObject(imgid);
    let icon;
    if (toggleVisibility(id, 'inline')) {
        icon= showimg;
    } else {
        icon = hideimg;
    }
    if(StringUtil.startsWith(icon,"fa-")) {
        $("#" + imgid).html(HtmlUtils.getIconImage(icon,[]));
    } else {
        if(img) img.obj.src = icon;
    }
    Utils.ramaddaUpdateMaps();
}


function toggleVisibility(id, style,anim) {
    let display = $("#" + id).css('display');
    $("#" + id).toggle(anim);
    return display != 'block';
}

function hide(id) {
    $("#" + id).hide();
}

function hideObject(obj) {
    if (!obj) {
        return 0;
    }
    $("#" + obj.id).hide();
    return 1;
}


function showObject(obj, display) {
    if (!obj) return 0;
    $("#" + obj.id).show();
    return;
}

function toggleVisibilityOnObject(obj, display) {
    if (!obj) return 0;
    $("#" + obj.id).toggle();
}






