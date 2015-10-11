function PopShow(o){
    $('.fixed_comment').animate({'bottom':'-60px'},200);
    $('.fixed_status').animate({'bottom':'-60px'},200);
    $('.'+o).animate({'bottom':0},200);
}
function PopHide(o){
    $('.'+o).animate({'bottom':'-60px'},200);
}

function statusSelect(){
    $('.fixed_status').find('li').click(function(){
        if($(this).attr("id") !== "lastBtn"){
            $('.fixed_status').find('li').removeClass('click');
            $(this).addClass('click');
        }
    })
}

function imgCenter(){
    var str = $('.taskRing_list_detail_info_img').find('.picture');
    var w1 = str.width();
    str.css({'height':w1+'px'});
    str.each(function(){
        var t = $(this).find('img');
        var img = new Image;
        img.src = t.attr("src");
        img.onload = function(){
            var imgH = img.height;
            var imgW = img.width;
            if(imgW > imgH){
                imgH = Math.ceil((w1*imgH)/imgW);
                var topH = (w1 - imgH)/2;
                t.css({'marginTop':topH+'px'});
            }else{
                imgW = Math.ceil((w1*imgW)/imgH);
                var topW = (w1 - imgW)/2;
                t.css({'width':'auto','height':w1+'px','marginLeft':topW+'px'});
            }

        };
    })
}