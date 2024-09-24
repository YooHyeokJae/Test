<%--
  Created by IntelliJ IDEA.
  User: yhj
  Date: 2024-09-04
  Time: 오후 2:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
<html>
<head>
    <title>Home</title>
</head>
<style>
    .docList {
        min-width: 10px;
        min-height: 10px;
    }
    .box{
        min-width: 150px;
        min-height: 40px;
    }
    .box.dragover{
        background-color: lightgray;
    }
    .dragover{
         background-color: lightgray;
     }
    .dragging {
        opacity: 0.5;
    }

    .possibleArea{
        background-color: skyblue;
    }

    /* hoverText */
    .hoverText {
        position: relative;
        border-bottom: 1px dotted black;
    }

    .hoverText:before {
        content: attr(data-hover);
        visibility: hidden;
        opacity: 0;
        width: max-content;
        background-color: black;
        color: #fff;
        text-align: center;
        border-radius: 5px;
        padding: 5px 5px;
        transition: opacity 1s ease-in-out;

        position: absolute;
        z-index: 1;
        left: 0;
        top: 110%;
    }

    .hoverText:hover:before {
        opacity: 1;
        visibility: visible;
    }
</style>
<body>
    <div class="container">
        <div class="row">
            <div class="col-12">
                <input type="file" id="inputFile" />
            </div>
        </div>

        <div class="row">
            <div class="col-4">
                <label for="docSearch" draggable="true">검색: <input type="text" id="docSearch" /></label>
                <br/>
                <label><input type="radio" name="filter" value="all" checked />전체보기</label>
                <label><input type="radio" name="filter" value="pos" />가능한 사람만 보기</label>
                <%--                need more search field like day or time--%>
                <ul id="docList" class="docList" style="max-height: 700px; overflow-y: auto; overflow-x: hidden"></ul>
            </div>
            <div class="col-8">
                <table>
                    <tr>
                        <th></th>
                        <th>1</th>
                        <th>2</th>
                        <th>3</th>
                        <th>4</th>
                        <th>5</th>
                        <th>6</th>
                    </tr>
                    <tr>
                        <th>-</th>
                        <td><div class="box" id="MonAM0"></div></td>
                        <td><div class="box" id="TueAM0"></div></td>
                        <td><div class="box" id="WedAM0"></div></td>
                        <td><div class="box" id="ThuAM0"></div></td>
                        <td><div class="box" id="FriAM0"></div></td>
                        <td><div class="box" id="SatAM0"></div></td>
                    </tr>
                    <tr>
                        <th>+</th>
                        <td><div class="box" id="MonPM0"></div></td>
                        <td><div class="box" id="TuePM0"></div></td>
                        <td><div class="box" id="WedPM0"></div></td>
                        <td><div class="box" id="ThuPM0"></div></td>
                        <td><div class="box" id="FriPM0"></div></td>
                        <td><div class="box" id="SatPM0"></div></td>
                    </tr>
                </table>
                <div class="remove">remove</div>
            </div>
        </div>
    </div>
</body>
<script>
    // ajax 리턴값 저장용 변수
    let content;

    // 파일 선택 시 실행
    $('#inputFile').on('change', function(){
        let formData = new FormData();
        let files = $(this)[0].files;
        formData.append("file", files[0]);

        $.ajax({
            url: '/fileProc1',
            processData: false,
            contentType: false,
            data: formData,
            type: 'post',
            success: function(result){
                content = result;
                let docListStr = '';
                for(let i=0; i<result.length; i++){
                    let hoverText = '';
                    let engText = '';
                    hoverText += result[i].subject + ' - ' + result[i].docName + ': ';
                    if(result[i].monAm !== '') { hoverText += '월오전 '; engText += 'MonAM '; }
                    if(result[i].monPm !== '') { hoverText += '월오후 '; engText += 'MonPM '; }
                    if(result[i].tueAm !== '') { hoverText += '화오전 '; engText += 'TueAM '; }
                    if(result[i].tuePm !== '') { hoverText += '화오후 '; engText += 'TuePM '; }
                    if(result[i].wedAm !== '') { hoverText += '수오전 '; engText += 'WedAM '; }
                    if(result[i].wedPm !== '') { hoverText += '수오후 '; engText += 'WedPM '; }
                    if(result[i].thuAm !== '') { hoverText += '목오전 '; engText += 'ThuAM '; }
                    if(result[i].thuPm !== '') { hoverText += '목오후 '; engText += 'ThuPM '; }
                    if(result[i].friAm !== '') { hoverText += '금오전 '; engText += 'FriAM '; }
                    if(result[i].friPm !== '') { hoverText += '금오후 '; engText += 'FriPM '; }
                    if(result[i].satAm !== '') { hoverText += '토오전 '; engText += 'SatAM '; }
                    if(result[i].satPm !== '') { hoverText += '토오후 '; engText += 'SatPM '; }

                    docListStr += '<li class="doctor hoverText" data-engText="' + engText + '" data-hover="'+hoverText+'" draggable="true" style="display: block">';
                    docListStr += result[i].subject + ' - ' + result[i].docName;
                    docListStr += '</li>'
                }
                $('#docList').html(docListStr);
            }
        });
    })

    let parentNodeId;
    let dragged;
    document.addEventListener("dragstart", (event) => {
        parentNodeId = event.target.parentNode.id;

        dragged = event.target;
        event.target.classList.add("dragging");

        let possibleArea = dragged.getAttribute("data-engText").split(" ");
        for(let i=0; i<possibleArea.length; i++){
            $('#'+possibleArea[i]+'0').addClass("possibleArea");
        }
    });
    document.addEventListener("dragover", (event) => event.preventDefault(), false, );
    document.addEventListener("dragend", (event) => {
        event.target.classList.remove("dragging");
        $('.box').removeClass("possibleArea");
    });
    document.addEventListener("dragenter", (event) => {
        if (event.target.classList.contains("box") || event.target.classList.contains("remove")) {
            event.target.classList.add("dragover");
        }
    });
    document.addEventListener("dragleave", (event) => {
        if (event.target.classList.contains("box") || event.target.classList.contains("remove")) {
            event.target.classList.remove("dragover");
        }
    });
    document.addEventListener("drop", (event) => {
        event.preventDefault();
        if (event.target.classList.contains("box")) {
            event.target.classList.remove("dragover");
            if(dragged.getAttribute("data-engText").indexOf(event.target.id.substring(0, 5)) === -1){
                alert('check the time!');
                return;
            }
            if(parentNodeId === 'docList'){
                let tbody = event.target.parentNode.parentNode.parentNode;
                for(let tr=1; tr<tbody.children.length; tr++){
                    for(let td=1; td<tbody.children[tr].children.length; td++){
                        for(let idx=0; idx<tbody.children[tr].children[td].children[0].children.length; idx++){
                            let chk = tbody.children[tr].children[td].children[0].children[idx].dataset.hover;
                            if(chk === dragged.dataset.hover){
                                alert('해당 의원은 이미 테이블에 추가하였습니다.');
                                return;
                            }
                        }
                    }
                }

                let newElem = dragged.cloneNode(true);
                newElem.innerText = newElem.innerText.split('- ')[1];
                newElem.style.width = "fit-content";
                newElem.classList.remove("dragging");
                event.target.appendChild(newElem);
            }else{
                dragged.parentNode.removeChild(dragged);
                event.target.appendChild(dragged);
            }
        } else if(event.target.classList.contains("remove")) {
            event.target.classList.remove("dragover");
            dragged.parentNode.removeChild(dragged);
            $('.box').removeClass("possibleArea");
        }
        if($('input[name="filter"]:checked').val() === 'pos'){
            checkTime();
        }
    });

    $('#docSearch').on('input', function(){
        let doctors = $('.doctor')
        if($(this).val().trim() === ''){
            doctors.css('display', 'block');
            return;
        }
        doctors.css('display', 'none');
        let keyword = $(this).val().trim();
        for(let i=0; i<doctors.length; i++){
            if(doctors.eq(i).parent().hasClass('box')){
                doctors.eq(i).css('display', 'block');
            }
            if(doctors.eq(i).text().indexOf(keyword) !== -1){
                doctors.eq(i).css('display', 'block');
            }
        }
    })

    $('input[name="filter"]').on('change', checkTime)

    function checkTime(){
        let chk = $('input[name="filter"]:checked').val();
        if(chk === 'all'){
            $('.doctor').css('display', 'block');
        }else{
            // 가능한 사람만 보기
            let exceptTime = [];
            let boxList = $('.box');
            for(let td=0; td<boxList.length; td++){
                if(boxList[td].hasChildNodes()){
                    exceptTime.push(boxList[td].id.substring(0, 5));
                }
            }

            let doctors = $('#docList').children();
            for(let docIdx=0; docIdx<doctors.length; docIdx++){
                let doctorTime = doctors[docIdx].getAttribute("data-engText");
                for(let timeIdx=0; timeIdx<exceptTime.length; timeIdx++){
                    let idx = doctorTime.indexOf(exceptTime[timeIdx]);
                    if(idx !== -1){
                        doctorTime = doctorTime.substring(0, idx) + doctorTime.substring(idx+5);
                    }
                }
                if(doctorTime.trim() === ''){
                    doctors[docIdx].style.display = 'none';
                }else{
                    doctors[docIdx].style.display = 'block';
                }
            }
        }
    }
</script>
</html>
