var zip = new JSZip();
var count = 0;
var mark = '%%_NAME_%%';
var correctName = 'KProp';

var domain = 'https://github.com/rmartinsanta/mork/tree/web/template';
var urls = [
  'asdfg.java',
  'asdasd/asdasd/asd.java',
  'asdasdasd.java'
];


urls.forEach(function(url){
  var filename = url;
  // loading a file and add it in a zip file
  JSZipUtils.getBinaryContent(domain + url, function (err, data) {
     if(err) {
        throw err; // or handle the error
     }
     zip.file(filename.replace(mark, correctName), data.replace(mark, correctName), {binary:true});
     count++;
     if (count == urls.length) {
       zip.generateAsync({type:'blob'}).then(function(content) {
          saveAs(content, correctName + ".zip");
       });
    }
  });
});
