/** @jsx ninja.siden.react.React.DOM */
var HelloMessage = ninja.siden.react.React.createClass({
  render: function() {
    return <div>Hello {this.props.name}</div>;
  }
});
