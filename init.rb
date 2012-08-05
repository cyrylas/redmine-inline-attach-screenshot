require 'application_patch'
require 'wiki_controller_patch'
require 'cleanup_tmp'
require_dependency 'attachment_hook'
if Gem::Version.new("3.0") > Gem::Version.new(Rails.version) then
  require 'dispatcher'
end

Redmine::Plugin.register :redmine_inline_attach_screenshot do
  name 'Redmine Attach Screenshot plugin'
  url 'https://bitbucket.org/StrangeWill/redmine-inline-attach-screenshot/'
  author 'Konstantin Zaitsev, Sergei Vasiliev, Alexandr Poplavsky, Axmor Software, Jens Alfke, Renzo Meister, William Roush'
  author_url 'https://bitbucket.org/StrangeWill/redmine-inline-attach-screenshot/'
  description 'Attach screenshots from clipboard directly to a Redmine issue.'
  version '0.4.2'
  
  if Gem::Version.new("3.0") > Gem::Version.new(Rails.version) then
    Dispatcher.to_prepare do
      ApplicationController.send(:include, AttachScreenshotPlugin::ApplicationControllerPatch)
      WikiController.send(:include, AttachScreenshotPlugin::WikiControllerPatch)
      AccountController.send(:include, AttachScreenshotPlugin::CleanupTmp)
    end
  else
    Rails.configuration.to_prepare do 
      ApplicationController.send(:include, AttachScreenshotPlugin::ApplicationControllerPatch)
      WikiController.send(:include, AttachScreenshotPlugin::WikiControllerPatch)
      AccountController.send(:include, AttachScreenshotPlugin::CleanupTmp)
    end
  end
end