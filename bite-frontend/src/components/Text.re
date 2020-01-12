module Raw = {
  [@react.component]
  let make =
      (
        ~content,
        ~variant=`Body1,
        ~color=?,
        ~className=?,
        ~noWrap=false,
        ~gutterBottom=false,
        ~style=?,
        (),
      ) => {
    <MaterialUi.Typography
      variant
      className={Cn.unpack(className)}
      noWrap
      gutterBottom
      ?color
      ?style>
      {React.string(content)}
    </MaterialUi.Typography>;
  };
};

[@react.component]
let make =
    (
      ~message,
      ~variant=`Body1,
      ~color=?,
      ~className=?,
      ~noWrap=false,
      ~gutterBottom=false,
      ~style=?,
      (),
    ) => {

  <Raw
    variant
    ?color
    ?className
    noWrap
    gutterBottom
    ?style
    content=message
  />;
};
