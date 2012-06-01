if Gem::Version.new("3.0") > Gem::Version.new(Rails.version) then
  ActionController::Routing::Routes.draw do |map|
    map.connect '/attach_screenshot', :controller => 'attach_screenshot', :action => 'index'
  end
else
  RedmineApp::Application.routes.draw do
    match 'attach_screenshot', :to => 'attach_screenshot#index'
  end
end