let utilFunc = {
    initIconFont () {
        // 避免重复注入
        if (document.getElementById('fontawesome-webfont')) {
            return;
        }
        var style = document.createElement('style');
        style.id = 'fontawesome-webfont';
        style.textContent = '@font-face { font-family: "fontawesome"; src: url("https://cdn.bootcss.com/font-awesome/4.7.0/fonts/fontawesome-webfont.ttf?v=5.10.2"); }';
        document.head.appendChild(style);
    }
};

export default utilFunc;
